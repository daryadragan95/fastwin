package com.striklewin.apps.feature.game.presentation

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.striklewin.apps.core.model.GameEvent
import com.striklewin.apps.core.model.GamePhase
import com.striklewin.apps.core.persistence.GamePreferencesRepository
import com.striklewin.apps.feature.game.domain.GameEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val preferencesRepository: GamePreferencesRepository,
    private val engine: GameEngine = GameEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _hapticEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 3)
    val hapticEvents: SharedFlow<Unit> = _hapticEvents.asSharedFlow()

    private var gameLoopJob: Job? = null

    init {
        observeHighScore()
        applySnapshot()
        startGameLoop()
    }

    fun onStartClicked() {
        engine.startNewRun()
        applySnapshot()
    }

    fun onPauseClicked() {
        engine.togglePause()
        applySnapshot()
    }

    fun onRestartClicked() {
        engine.startNewRun()
        applySnapshot()
    }

    fun onPlayerDragged(normalizedX: Float) {
        engine.movePlayer(normalizedX)
        applySnapshot()
    }

    private fun observeHighScore() {
        viewModelScope.launch {
            preferencesRepository.highScoreFlow.collect { highScore ->
                _uiState.update { current -> current.copy(highScore = highScore) }
            }
        }
    }

    private fun startGameLoop() {
        if (gameLoopJob?.isActive == true) return
        gameLoopJob = viewModelScope.launch {
            var previousFrameTime = SystemClock.elapsedRealtime()
            while (isActive) {
                val currentTime = SystemClock.elapsedRealtime()
                val delta = currentTime - previousFrameTime
                previousFrameTime = currentTime

                val outcome = engine.tick(delta)
                applySnapshot(outcome.snapshot)
                handleEvents(outcome.events)

                delay(16L)
            }
        }
    }

    private fun handleEvents(events: Set<GameEvent>) {
        if (events.isEmpty()) return

        if (GameEvent.GAME_OVER in events || GameEvent.SHIELD_BROKEN in events) {
            _hapticEvents.tryEmit(Unit)
        }

        if (GameEvent.GAME_OVER in events) {
            val current = _uiState.value
            if (current.score > current.highScore) {
                viewModelScope.launch {
                    preferencesRepository.saveHighScore(current.score)
                }
            }
        }
    }

    private fun applySnapshot() {
        applySnapshot(engine.snapshot())
    }

    private fun applySnapshot(snapshot: com.striklewin.apps.core.model.GameSnapshot) {
        _uiState.update { current ->
            current.copy(
                phase = snapshot.phase,
                score = snapshot.score,
                distanceMeters = snapshot.distanceMeters,
                playerX = snapshot.playerX,
                playerY = snapshot.playerY,
                playerRadius = snapshot.playerRadius,
                obstacles = snapshot.obstacles,
                bonuses = snapshot.bonuses,
                shieldRemainingMs = snapshot.shieldRemainingMs,
                multiplierRemainingMs = snapshot.multiplierRemainingMs
            )
        }
    }

    companion object {
        fun factory(repository: GamePreferencesRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(GameViewModel::class.java)) {
                        "Unknown ViewModel class: ${modelClass.name}"
                    }
                    return GameViewModel(repository) as T
                }
            }
        }
    }
}

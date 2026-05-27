package com.striklewin.apps.feature.game.presentation

import com.striklewin.apps.core.model.BonusItem
import com.striklewin.apps.core.model.GamePhase
import com.striklewin.apps.core.model.Obstacle

data class GameUiState(
    val phase: GamePhase = GamePhase.READY,
    val score: Int = 0,
    val highScore: Int = 0,
    val distanceMeters: Int = 0,
    val playerX: Float = 0.5f,
    val playerY: Float = 0.88f,
    val playerRadius: Float = 0.05f,
    val obstacles: List<Obstacle> = emptyList(),
    val bonuses: List<BonusItem> = emptyList(),
    val shieldRemainingMs: Long = 0L,
    val multiplierRemainingMs: Long = 0L
)

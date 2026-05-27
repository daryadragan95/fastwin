package com.striklewin.apps.feature.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.striklewin.apps.core.model.BonusType
import com.striklewin.apps.core.model.GamePhase
import com.striklewin.apps.core.model.ObstacleType
import com.striklewin.apps.feature.game.presentation.GameUiState
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun GameScreen(
    uiState: GameUiState,
    hapticEvents: SharedFlow<Unit>,
    onStartClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onRestartClicked: () -> Unit,
    onPlayerDragged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    HapticCollector(
        events = hapticEvents,
        onEvent = { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress) }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF06243A),
                        Color(0xFF0B3B5C),
                        Color(0xFF113A28)
                    )
                )
            )
    ) {
        var widthPx by remember { mutableFloatStateOf(1f) }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.phase) {
                    detectDragGestures { change, _ ->
                        val normalizedX = (change.position.x / widthPx).coerceIn(0f, 1f)
                        onPlayerDragged(normalizedX)
                    }
                }
        ) {
            widthPx = size.width
            val minDimension = size.minDimension

            val laneColor = Color.White.copy(alpha = 0.10f)
            val laneXs = listOf(0.2f, 0.35f, 0.5f, 0.65f, 0.8f)
            laneXs.forEach { laneX ->
                drawLine(
                    color = laneColor,
                    start = Offset(laneX * size.width, 0f),
                    end = Offset(laneX * size.width, size.height),
                    strokeWidth = 3f
                )
            }

            drawCircle(
                color = Color(0xFFFFA629),
                radius = uiState.playerRadius * minDimension,
                center = Offset(uiState.playerX * size.width, uiState.playerY * size.height)
            )

            uiState.obstacles.forEach { obstacle ->
                val center = Offset(obstacle.x * size.width, obstacle.y * size.height)
                val radius = obstacle.radius * minDimension
                when (obstacle.type) {
                    ObstacleType.CONE -> {
                        val path = Path().apply {
                            moveTo(center.x, center.y - radius)
                            lineTo(center.x - radius, center.y + radius)
                            lineTo(center.x + radius, center.y + radius)
                            close()
                        }
                        drawPath(path = path, color = Color(0xFFFF6D3A), style = Fill)
                    }

                    ObstacleType.DEFENDER -> {
                        drawCircle(color = Color(0xFFE64848), radius = radius, center = center)
                        drawCircle(color = Color(0xFFF6E8E8), radius = radius * 0.35f, center = center)
                    }

                    ObstacleType.PIT -> {
                        drawCircle(color = Color(0xFF1A1A1A), radius = radius, center = center)
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.55f),
                            radius = radius * 0.65f,
                            center = center
                        )
                    }
                }
            }

            uiState.bonuses.forEach { bonus ->
                val center = Offset(bonus.x * size.width, bonus.y * size.height)
                val radius = bonus.radius * minDimension
                when (bonus.type) {
                    BonusType.SHIELD -> {
                        drawCircle(color = Color(0xFF44E3FF), radius = radius, center = center)
                        drawCircle(color = Color.White.copy(alpha = 0.45f), radius = radius * 0.55f, center = center)
                    }

                    BonusType.SCORE_X2 -> {
                        drawCircle(color = Color(0xFF8BFF44), radius = radius, center = center)
                        drawCircle(color = Color(0xFF2D5D00), radius = radius * 0.45f, center = center)
                    }
                }
            }
        }

        TopHud(
            score = uiState.score,
            highScore = uiState.highScore,
            distanceMeters = uiState.distanceMeters,
            shieldRemainingMs = uiState.shieldRemainingMs,
            multiplierRemainingMs = uiState.multiplierRemainingMs,
            phase = uiState.phase,
            onPauseClicked = onPauseClicked,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        when (uiState.phase) {
            GamePhase.READY -> {
                OverlayCard(
                    title = "Dribble Master",
                    body = "Обходи препятствия и собирай бонусы.\nУправление: веди мяч пальцем по экрану.",
                    buttonText = "Старт",
                    onButtonClick = onStartClicked,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            GamePhase.GAME_OVER -> {
                OverlayCard(
                    title = "Игра окончена",
                    body = "Счет: ${uiState.score}\nРекорд: ${uiState.highScore}",
                    buttonText = "Сыграть еще",
                    onButtonClick = onRestartClicked,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            GamePhase.PAUSED -> {
                OverlayCard(
                    title = "Пауза",
                    body = "Нажми продолжить, чтобы вернуться в игру.",
                    buttonText = "Продолжить",
                    onButtonClick = onPauseClicked,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            GamePhase.RUNNING -> Unit
        }
    }
}

@Composable
private fun TopHud(
    score: Int,
    highScore: Int,
    distanceMeters: Int,
    shieldRemainingMs: Long,
    multiplierRemainingMs: Long,
    phase: GamePhase,
    onPauseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Счет: $score",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Рекорд: $highScore",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            if (phase == GamePhase.RUNNING) {
                Button(onClick = onPauseClicked) {
                    Text("Пауза")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatPill(label = "Дистанция", value = "$distanceMeters м")
            StatPill(label = "Щит", value = "${shieldRemainingMs / 1000}s")
            StatPill(label = "x2", value = "${multiplierRemainingMs / 1000}s")
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun OverlayCard(
    title: String,
    body: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(20.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF081B2A).copy(alpha = 0.93f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.88f)
            )
            Button(onClick = onButtonClick, modifier = Modifier.fillMaxWidth()) {
                Text(buttonText)
            }
        }
    }
}

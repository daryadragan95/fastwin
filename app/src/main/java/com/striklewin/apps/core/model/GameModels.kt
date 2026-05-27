package com.striklewin.apps.core.model

enum class GamePhase {
    READY,
    RUNNING,
    PAUSED,
    GAME_OVER
}

enum class ObstacleType {
    CONE,
    DEFENDER,
    PIT
}

data class Obstacle(
    val id: Long,
    val type: ObstacleType,
    val x: Float,
    val y: Float,
    val radius: Float
)

enum class BonusType {
    SHIELD,
    SCORE_X2
}

data class BonusItem(
    val id: Long,
    val type: BonusType,
    val x: Float,
    val y: Float,
    val radius: Float
)

data class GameSnapshot(
    val phase: GamePhase,
    val score: Int,
    val distanceMeters: Int,
    val playerX: Float,
    val playerY: Float,
    val playerRadius: Float,
    val obstacles: List<Obstacle>,
    val bonuses: List<BonusItem>,
    val shieldRemainingMs: Long,
    val multiplierRemainingMs: Long
)

enum class GameEvent {
    SHIELD_BROKEN,
    BONUS_COLLECTED,
    GAME_OVER
}

data class TickOutcome(
    val snapshot: GameSnapshot,
    val events: Set<GameEvent>
)

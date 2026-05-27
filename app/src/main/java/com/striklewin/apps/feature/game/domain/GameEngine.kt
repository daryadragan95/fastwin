package com.striklewin.apps.feature.game.domain

import com.striklewin.apps.core.model.BonusItem
import com.striklewin.apps.core.model.BonusType
import com.striklewin.apps.core.model.GameEvent
import com.striklewin.apps.core.model.GamePhase
import com.striklewin.apps.core.model.GameSnapshot
import com.striklewin.apps.core.model.Obstacle
import com.striklewin.apps.core.model.ObstacleType
import com.striklewin.apps.core.model.TickOutcome
import kotlin.math.sqrt
import kotlin.random.Random

class GameEngine(
    private val random: Random = Random(System.currentTimeMillis())
) {

    companion object {
        const val PLAYER_Y = 0.88f
        private const val PLAYER_RADIUS = 0.05f
        private const val OBJECT_RADIUS = 0.05f
        private const val BASE_SPEED = 0.62f
        private const val SPEED_GROWTH_PER_SECOND = 0.012f
        private const val MIN_X = 0.12f
        private const val MAX_X = 0.88f
        private const val SHIELD_DURATION_MS = 3_000L
        private const val MULTIPLIER_DURATION_MS = 5_000L
        private const val BONUS_INTERVAL_MS = 6_000L
    }

    private var phase = GamePhase.READY
    private var score = 0
    private var distanceMeters = 0
    private var playerX = 0.5f

    private var shieldRemainingMs = 0L
    private var multiplierRemainingMs = 0L

    private val obstacles = mutableListOf<Obstacle>()
    private val bonuses = mutableListOf<BonusItem>()

    private var nextId = 1L
    private var elapsedMs = 0L
    private var obstacleTimerMs = 0L
    private var bonusTimerMs = 0L
    private var distanceAccumulator = 0f
    private var scoreAccumulator = 0f

    fun startNewRun() {
        phase = GamePhase.RUNNING
        score = 0
        distanceMeters = 0
        playerX = 0.5f
        shieldRemainingMs = 0L
        multiplierRemainingMs = 0L
        obstacles.clear()
        bonuses.clear()
        elapsedMs = 0L
        obstacleTimerMs = 0L
        bonusTimerMs = 0L
        distanceAccumulator = 0f
        scoreAccumulator = 0f
    }

    fun togglePause() {
        phase = when (phase) {
            GamePhase.RUNNING -> GamePhase.PAUSED
            GamePhase.PAUSED -> GamePhase.RUNNING
            else -> phase
        }
    }

    fun movePlayer(normalizedX: Float) {
        playerX = normalizedX.coerceIn(MIN_X, MAX_X)
    }

    fun tick(deltaMsRaw: Long): TickOutcome {
        val deltaMs = deltaMsRaw.coerceIn(0L, 64L)
        if (phase != GamePhase.RUNNING) {
            return TickOutcome(snapshot(), emptySet())
        }

        val events = mutableSetOf<GameEvent>()
        elapsedMs += deltaMs

        if (shieldRemainingMs > 0) {
            shieldRemainingMs = (shieldRemainingMs - deltaMs).coerceAtLeast(0L)
        }
        if (multiplierRemainingMs > 0) {
            multiplierRemainingMs = (multiplierRemainingMs - deltaMs).coerceAtLeast(0L)
        }

        val speed = BASE_SPEED + (elapsedMs / 1_000f) * SPEED_GROWTH_PER_SECOND
        val dy = speed * deltaMs / 1_000f

        obstacleTimerMs += deltaMs
        bonusTimerMs += deltaMs

        val obstacleInterval = (900L - elapsedMs / 100).coerceAtLeast(320L)
        while (obstacleTimerMs >= obstacleInterval) {
            obstacleTimerMs -= obstacleInterval
            obstacles += Obstacle(
                id = nextId++,
                type = ObstacleType.entries.random(random),
                x = randomLaneX(),
                y = -0.10f,
                radius = OBJECT_RADIUS
            )
        }

        while (bonusTimerMs >= BONUS_INTERVAL_MS) {
            bonusTimerMs -= BONUS_INTERVAL_MS
            bonuses += BonusItem(
                id = nextId++,
                type = BonusType.entries.random(random),
                x = randomLaneX(),
                y = -0.10f,
                radius = OBJECT_RADIUS
            )
        }

        moveObjects(dy)
        cleanupOffscreen()
        processBonusCollisions(events)

        if (phase == GamePhase.RUNNING) {
            processObstacleCollisions(events)
        }

        if (phase == GamePhase.RUNNING) {
            val multiplier = if (multiplierRemainingMs > 0) 2f else 1f
            scoreAccumulator += deltaMs / 45f * multiplier
            val scoreDelta = scoreAccumulator.toInt()
            if (scoreDelta > 0) {
                score += scoreDelta
                scoreAccumulator -= scoreDelta
            }

            distanceAccumulator += speed * deltaMs * 0.035f
            val metersDelta = distanceAccumulator.toInt()
            if (metersDelta > 0) {
                distanceMeters += metersDelta
                distanceAccumulator -= metersDelta
            }
        }

        return TickOutcome(snapshot(), events)
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        phase = phase,
        score = score,
        distanceMeters = distanceMeters,
        playerX = playerX,
        playerY = PLAYER_Y,
        playerRadius = PLAYER_RADIUS,
        obstacles = obstacles.toList(),
        bonuses = bonuses.toList(),
        shieldRemainingMs = shieldRemainingMs,
        multiplierRemainingMs = multiplierRemainingMs
    )

    private fun moveObjects(dy: Float) {
        for (index in obstacles.indices) {
            val old = obstacles[index]
            obstacles[index] = old.copy(y = old.y + dy)
        }
        for (index in bonuses.indices) {
            val old = bonuses[index]
            bonuses[index] = old.copy(y = old.y + dy)
        }
    }

    private fun cleanupOffscreen() {
        obstacles.removeAll { it.y - it.radius > 1.15f }
        bonuses.removeAll { it.y - it.radius > 1.15f }
    }

    private fun processBonusCollisions(events: MutableSet<GameEvent>) {
        val iterator = bonuses.iterator()
        while (iterator.hasNext()) {
            val bonus = iterator.next()
            if (isCollision(playerX, PLAYER_Y, PLAYER_RADIUS, bonus.x, bonus.y, bonus.radius)) {
                when (bonus.type) {
                    BonusType.SHIELD -> shieldRemainingMs = SHIELD_DURATION_MS
                    BonusType.SCORE_X2 -> multiplierRemainingMs = MULTIPLIER_DURATION_MS
                }
                iterator.remove()
                events += GameEvent.BONUS_COLLECTED
            }
        }
    }

    private fun processObstacleCollisions(events: MutableSet<GameEvent>) {
        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obstacle = iterator.next()
            if (isCollision(playerX, PLAYER_Y, PLAYER_RADIUS, obstacle.x, obstacle.y, obstacle.radius)) {
                if (shieldRemainingMs > 0) {
                    shieldRemainingMs = 0L
                    iterator.remove()
                    events += GameEvent.SHIELD_BROKEN
                } else {
                    phase = GamePhase.GAME_OVER
                    events += GameEvent.GAME_OVER
                    return
                }
            }
        }
    }

    private fun randomLaneX(): Float {
        val lanes = floatArrayOf(0.2f, 0.35f, 0.5f, 0.65f, 0.8f)
        return lanes[random.nextInt(lanes.size)]
    }

    private fun isCollision(
        x1: Float,
        y1: Float,
        r1: Float,
        x2: Float,
        y2: Float,
        r2: Float
    ): Boolean {
        val dx = x1 - x2
        val dy = y1 - y2
        val distance = sqrt(dx * dx + dy * dy)
        return distance <= (r1 + r2)
    }
}

package com.striklewin.apps.feature.game.domain

import com.striklewin.apps.core.model.GamePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun `startNewRun switches engine to running`() {
        val engine = GameEngine()

        engine.startNewRun()

        assertEquals(GamePhase.RUNNING, engine.snapshot().phase)
    }

    @Test
    fun `tick increases score during running`() {
        val engine = GameEngine()
        engine.startNewRun()

        repeat(30) {
            engine.tick(16L)
        }

        assertTrue(engine.snapshot().score > 0)
    }
}

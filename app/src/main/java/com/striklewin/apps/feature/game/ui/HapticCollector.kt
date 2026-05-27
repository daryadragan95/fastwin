package com.striklewin.apps.feature.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun HapticCollector(
    events: SharedFlow<Unit>,
    onEvent: () -> Unit
) {
    LaunchedEffect(events) {
        events.collect {
            onEvent()
        }
    }
}

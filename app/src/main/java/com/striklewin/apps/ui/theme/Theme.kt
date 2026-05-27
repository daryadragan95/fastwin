package com.striklewin.apps.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DribbleColorScheme = darkColorScheme(
    primary = Color(0xFFFFA629),
    onPrimary = Color(0xFF1A1300),
    secondary = Color(0xFF44E3FF),
    onSecondary = Color(0xFF001E26),
    tertiary = Color(0xFF8BFF44),
    background = Color(0xFF06243A),
    surface = Color(0xFF0C2F48),
    onBackground = Color(0xFFF2F7FB),
    onSurface = Color(0xFFF2F7FB)
)

@Composable
fun DribbleMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DribbleColorScheme,
        typography = Typography,
        content = content
    )
}

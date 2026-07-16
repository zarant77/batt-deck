package com.catemup.battdeck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val scheme = darkColorScheme(
    primary = UkrainianBlue,
    secondary = UkrainianYellow,
    tertiary = ReadyGreen,
    error = DangerRed,
    background = Background,
    surface = Panel,
    surfaceVariant = Grid,
    onPrimary = TextPrimary,
    onSecondary = Background,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextMuted,
)

@Composable
fun BattDeckTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}

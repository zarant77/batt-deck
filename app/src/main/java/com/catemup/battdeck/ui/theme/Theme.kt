package com.catemup.battdeck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

private val scheme = darkColorScheme(primary = NeonGreen, secondary = WarningOrange, error = DangerRed, background = Background, surface = Panel, onPrimary = Background, onBackground = TextPrimary, onSurface = TextPrimary)
private val type = Typography().run {
    copy(displayLarge = displayLarge.copy(fontFamily = FontFamily.Monospace), headlineMedium = headlineMedium.copy(fontFamily = FontFamily.Monospace), titleLarge = titleLarge.copy(fontFamily = FontFamily.Monospace), bodyLarge = bodyLarge.copy(fontFamily = FontFamily.Monospace), bodyMedium = bodyMedium.copy(fontFamily = FontFamily.Monospace), labelLarge = labelLarge.copy(fontFamily = FontFamily.Monospace))
}
@Composable fun BattDeckTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = scheme, typography = type, content = content) }

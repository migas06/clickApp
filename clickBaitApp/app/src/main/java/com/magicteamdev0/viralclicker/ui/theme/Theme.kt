package com.magicteamdev0.viralclicker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = NeonPink,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = OnDark,
    onSecondary = OnDark,
    onTertiary = OnDark,
    onBackground = OnDark,
    onSurface = OnDark,
    onSurfaceVariant = OnDarkSecondary,
    error = NeonPink
)

@Composable
fun ViralClickerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

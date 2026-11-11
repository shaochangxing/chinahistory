package com.sqz.historytimeline.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Color(0xFF9E3B2B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF2DAD3),
    onPrimaryContainer = Color(0xFF3B0C05),
    secondary = Color(0xFF7A5C34),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F0E6),
    onBackground = Color(0xFF231E19),
    surface = Color(0xFFFBF7EF),
    onSurface = Color(0xFF231E19),
    surfaceVariant = Color(0xFFEDE3D2),
    onSurfaceVariant = Color(0xFF5C5245),
    outline = Color(0xFFB6A78F),
    outlineVariant = Color(0xFFDED3C0),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFDB8B78),
    onPrimary = Color(0xFF42120A),
    primaryContainer = Color(0xFF5E2318),
    onPrimaryContainer = Color(0xFFF2DAD3),
    secondary = Color(0xFFC9A874),
    onSecondary = Color(0xFF2B1D07),
    background = Color(0xFF12100D),
    onBackground = Color(0xFFEAE2D4),
    surface = Color(0xFF1A1713),
    onSurface = Color(0xFFEAE2D4),
    surfaceVariant = Color(0xFF26211A),
    onSurfaceVariant = Color(0xFFB9AE9C),
    outline = Color(0xFF6B6050),
    outlineVariant = Color(0xFF342E25),
)

@Composable
fun HistoryTimelineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}

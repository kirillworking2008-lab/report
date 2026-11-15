package com.sterzhen.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF7043),
    secondary = Color(0xFFFFB74D),
    background = Color(0xFF0E0E13),
    surface = Color(0xFF1A1A22),
    surfaceVariant = Color(0xFF26262F),
    onBackground = Color(0xFFF5F5F7),
    onSurface = Color(0xFFF5F5F7),
    onSurfaceVariant = Color(0xFFB8B8C0)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFE64A19),
    secondary = Color(0xFFFF8A65),
    background = Color(0xFFFFFBF5),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1E8E0),
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF6B6B70)
)

@Composable
fun SterzhenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

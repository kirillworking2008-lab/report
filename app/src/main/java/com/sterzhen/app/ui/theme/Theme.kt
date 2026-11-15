package com.sterzhen.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Палитра «тёмное стекло + уголёк». */
object Ink {
    val Bg = Color(0xFF07070B)
    val BgSoft = Color(0xFF0C0C13)
    val Card = Color(0xFF14141E)
    val CardHi = Color(0xFF1D1D2A)
    val Stroke = Color(0x1FFFFFFF)
    val Text = Color(0xFFF7F7FA)
    val TextDim = Color(0xFF9C9CAC)
    val TextFaint = Color(0xFF5C5C6B)
    val Ember = Color(0xFFFF7A2F)
    val EmberDeep = Color(0xFFFF3D6E)
    val Gold = Color(0xFFFFC46B)
    val Mint = Color(0xFF4ADE9B)
    val Danger = Color(0xFFFF5566)
    val Ice = Color(0xFF6EA8FF)
}

private val Scheme = darkColorScheme(
    primary = Ink.Ember,
    onPrimary = Color(0xFF1A0A02),
    primaryContainer = Color(0xFF3A1A08),
    onPrimaryContainer = Ink.Gold,
    secondary = Ink.Gold,
    onSecondary = Color(0xFF231602),
    tertiary = Ink.Mint,
    background = Ink.Bg,
    onBackground = Ink.Text,
    surface = Ink.Card,
    onSurface = Ink.Text,
    surfaceVariant = Ink.CardHi,
    onSurfaceVariant = Ink.TextDim,
    outline = Ink.Stroke,
    error = Ink.Danger
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 52.sp,
        lineHeight = 54.sp,
        letterSpacing = (-2).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 38.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.6).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.6.sp
    )
)

@Composable
fun SterzhenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = AppTypography,
        content = content
    )
}

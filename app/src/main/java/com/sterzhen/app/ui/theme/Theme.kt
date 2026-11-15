package com.sterzhen.app.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Палитра из дизайн-системы Running v1.0 (Running-Design-Kit.zip).
 *
 * Тёмное «стекло»: фон #080D12, панели — полупрозрачный белый,
 * один акцент на экран. Акценты кита: mint #73E3CF (основной),
 * violet #B9A4FF, amber #FFB376.
 *
 * Имена оставлены прежними, чтобы не ломать существующий код,
 * но значения — строго из кита.
 */
object Ink {
    // Фон и поверхности
    val Bg = Color(0xFF080D12)
    val BgSoft = Color(0xFF0A1117)
    val Card = Color(0xFF0E171D)
    val CardHi = Color(0xFF16232A)
    val Panel = Color(0x0BFFFFFF)      // rgba(255,255,255,.045)
    val Stroke = Color(0x16FFFFFF)     // rgba(255,255,255,.085)
    val Field = Color(0x7303080C)      // rgba(3,8,12,.45)

    // Текст
    val Text = Color(0xFFF4F7F8)       // --ink
    val TextDim = Color(0xFF849199)    // --muted
    val TextFaint = Color(0xFF56636B)  // --faint

    // Акценты кита
    val Ember = Color(0xFF73E3CF)      // основной акцент — mint
    val EmberDeep = Color(0xFF9AF0E2)  // mint + 16% белого (для градиентов)
    val Mint = Color(0xFF73E3CF)
    val Violet = Color(0xFFB9A4FF)
    val VioletDeep = Color(0xFF30264E)
    val Gold = Color(0xFFFFB376)       // amber
    val AmberDeep = Color(0xFF503521)
    val AccentDeep = Color(0xFF173B3B) // --accent-deep
    val Danger = Color(0xFFFF8484)     // тот же invalid-цвет, что в ките
    val Ice = Color(0xFF849199)        // в ките синего нет — используем muted
}

/** Скругления кита: 17 / 14 / 16 / 26 / 30 px по типу элемента. */
object Radii {
    val Card = 17.dp
    val Field = 14.dp
    val Button = 16.dp
    val Hero = 26.dp
    val Sheet = 26.dp
    val Menu = 30.dp
}

private val Scheme = darkColorScheme(
    primary = Ink.Ember,
    onPrimary = Color(0xFF101716),
    primaryContainer = Ink.AccentDeep,
    onPrimaryContainer = Ink.Ember,
    secondary = Ink.Violet,
    onSecondary = Color(0xFF1A1430),
    tertiary = Ink.Gold,
    background = Ink.Bg,
    onBackground = Ink.Text,
    surface = Ink.Card,
    onSurface = Ink.Text,
    surfaceVariant = Ink.CardHi,
    onSurfaceVariant = Ink.TextDim,
    outline = Ink.Stroke,
    error = Ink.Danger
)

/**
 * Типографика кита: Inter/system-ui, плотные отрицательные трекинги
 * в заголовках (-0.045em), tabular-цифры для метрик, подписи 9sp uppercase.
 */
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(680),
        fontSize = 52.sp,
        lineHeight = 52.sp,
        letterSpacing = (-2.34).sp,
        fontFeatureSettings = "tnum"
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(680),
        fontSize = 38.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1.71).sp,
        fontFeatureSettings = "tnum"
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(680),
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-1.26).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(680),
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.9).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(680),
        fontSize = 16.sp,
        letterSpacing = (-0.72).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(570),
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(500),
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(680),
        fontSize = 14.sp,
        letterSpacing = (-0.14).sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(650),
        fontSize = 11.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight(690),
        fontSize = 9.sp,
        letterSpacing = 0.81.sp
    )
)

@Composable
fun SterzhenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = AppTypography
    ) {
        // Без этого Text без явного цвета рисуется чёрным (дефолт LocalContentColor)
        CompositionLocalProvider(LocalContentColor provides Ink.Text, content = content)
    }
}

package com.sterzhen.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sterzhen.app.ui.theme.Ink
import com.sterzhen.app.ui.theme.Radii

/**
 * Цвет огонька по длине серии — только акценты дизайн-кита:
 * холодный faint → ink → mint → amber → violet.
 */
fun flameColor(streak: Int): Color = when {
    streak >= 100 -> Ink.Violet
    streak >= 30 -> Ink.Gold
    streak >= 7 -> Ink.Mint
    streak > 0 -> Ink.Text
    else -> Ink.TextFaint
}

/** Живой фон: медленно дрейфующие цветные пятна. */
@Composable
fun AuroraBackground(content: @Composable BoxScope.() -> Unit) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Box(modifier = Modifier.fillMaxSize().background(Ink.Bg)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r1 = size.maxDimension * 0.75f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Ink.Ember.copy(alpha = 0.075f), Color.Transparent),
                    center = Offset(size.width * (0.10f + 0.30f * t), size.height * 0.06f),
                    radius = r1
                ),
                radius = r1,
                center = Offset(size.width * (0.10f + 0.30f * t), size.height * 0.06f)
            )
            val r2 = size.maxDimension * 0.65f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Ink.Violet.copy(alpha = 0.05f), Color.Transparent),
                    center = Offset(size.width * (1.05f - 0.25f * t), size.height * 0.45f),
                    radius = r2
                ),
                radius = r2,
                center = Offset(size.width * (1.05f - 0.25f * t), size.height * 0.45f)
            )
            val r3 = size.maxDimension * 0.7f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Ink.Gold.copy(alpha = 0.045f), Color.Transparent),
                    center = Offset(size.width * (0.2f + 0.5f * (1f - t)), size.height * 1.0f),
                    radius = r3
                ),
                radius = r3,
                center = Offset(size.width * (0.2f + 0.5f * (1f - t)), size.height * 1.0f)
            )
        }
        content()
    }
}

/** Стеклянная карточка с тонкой светящейся рамкой. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(Radii.Card),
    glow: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Ink.CardHi, Ink.Card)))
            // стекло кита: linear-gradient(145deg, rgba(255,255,255,.055), rgba(255,255,255,.022))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.055f),
                        Color.White.copy(alpha = 0.022f)
                    )
                )
            )
            // точечное свечение акцента в углу, если задано
            .background(
                if (glow == null) Brush.linearGradient(
                    listOf(Color.Transparent, Color.Transparent)
                ) else Brush.radialGradient(
                    colors = listOf(glow.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 520f
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.16f),          // внутренняя светлая линия
                        (glow ?: Color.White).copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape
            )
            .padding(18.dp),
        content = content
    )
}

/** Кольцо прогресса вокруг эмодзи: сколько дней держится принцип. */
@Composable
fun StreakRing(
    streak: Int,
    color: Color,
    emoji: String,
    dimension: Dp = 46.dp,
    strokeWidth: Dp = 3.dp
) {
    val target = (streak.coerceIn(0, 30)) / 30f
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "ring"
    )
    Box(modifier = Modifier.size(dimension), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Color.White.copy(alpha = 0.07f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(color.copy(alpha = 0.25f), color, color.copy(alpha = 0.65f))
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        Text(text = emoji, fontSize = (dimension.value * 0.42f).sp)
    }
}

/** Пульсирующий огонёк с дымкой. */
@Composable
fun FlameBurst(streak: Int, dimension: Dp = 56.dp, emoji: String = "🔥") {
    val transition = rememberInfiniteTransition(label = "flame")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val color = flameColor(streak)

    Box(modifier = Modifier.size(dimension), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                    alpha = if (streak > 0) 1f else 0.35f
                }
        ) {
            val radius = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.45f), color.copy(alpha = 0.08f), Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
        Text(
            text = if (streak > 0) emoji else "🧊",
            fontSize = (dimension.value * 0.42f).sp
        )
    }
}

/** Крупное число стрика с плавным счётом. */
@Composable
fun BigStreak(streak: Int, fontSize: Int = 52, color: Color = Ink.Text) {
    val animated by animateIntAsState(
        targetValue = streak,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "streak"
    )
    Text(
        text = "$animated",
        style = TextStyle(
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-2).sp,
            brush = Brush.verticalGradient(listOf(color, color.copy(alpha = 0.55f)))
        )
    )
}

/** Подпись секции: мелкий капс + заголовок. */
@Composable
fun SectionHeader(title: String, caption: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Ink.TextFaint
        )
        if (caption != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = Ink.TextDim
            )
        }
    }
}

/** Полоски истории: 14 дней, от старых к новым. */
@Composable
fun DayStrip(days: List<Boolean?>, modifier: Modifier = Modifier, barHeight: Dp = 20.dp) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEachIndexed { index, day ->
            val isLast = index == days.lastIndex
            val color = when (day) {
                true -> if (isLast) Ink.Mint else Ink.Mint.copy(alpha = 0.45f)
                false -> Ink.Danger
                null -> Color.White.copy(alpha = 0.07f)
            }
            Box(
                modifier = Modifier
                    .width(if (isLast) 7.dp else 5.dp)
                    .height(if (isLast) barHeight else barHeight * 0.78f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

/** Компактная метрика: значение + подпись. */
@Composable
fun Stat(label: String, value: String, accent: Color = Ink.Text, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = accent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Ink.TextDim
        )
    }
}

/** Пилюля-метка. */
@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.30f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/** Главная кнопка с огненным градиентом. */
@Composable
fun GlowButton(
    text: String,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(Ink.Ember, Ink.EmberDeep),
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radii.Button))
            .background(Brush.linearGradient(colors))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(Radii.Button))
            .clickable(onClick = onClick)
            .padding(vertical = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF101716)
        )
    }
}

/** Кнопка-контур. */
@Composable
fun OutlineButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Ink.TextDim,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

/** Прогресс до следующей вехи: неделя → месяц → 100 дней → год. */
@Composable
fun MilestoneBar(streak: Int, color: Color, modifier: Modifier = Modifier) {
    val milestones = listOf(7, 30, 100, 365)
    val next = milestones.firstOrNull { it > streak } ?: (((streak / 365) + 1) * 365)
    val prev = milestones.lastOrNull { it <= streak } ?: 0
    val fraction = if (next > prev) {
        ((streak - prev).toFloat() / (next - prev)).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animated by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "milestone"
    )
    val label = when (next) {
        7 -> "неделя"
        30 -> "месяц"
        100 -> "100 дней"
        365 -> "год"
        else -> "$next дней"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$streak",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "$next · $label",
                style = MaterialTheme.typography.bodySmall,
                color = Ink.TextFaint
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.78f)))
                    )
            )
        }
    }
}

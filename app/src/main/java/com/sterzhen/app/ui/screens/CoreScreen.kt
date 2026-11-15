package com.sterzhen.app.ui.screens

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.sterzhen.app.data.Principle
import com.sterzhen.app.data.Principles
import com.sterzhen.app.ui.AppViewModel
import com.sterzhen.app.ui.PrincipleState
import com.sterzhen.app.ui.components.BigStreak
import com.sterzhen.app.ui.components.DayStrip
import com.sterzhen.app.ui.components.GlassCard
import com.sterzhen.app.ui.components.MilestoneBar
import com.sterzhen.app.ui.components.StartDateDialog
import com.sterzhen.app.ui.components.OutlineButton
import com.sterzhen.app.ui.components.Pill
import com.sterzhen.app.ui.components.SectionHeader
import com.sterzhen.app.ui.components.StreakRing
import com.sterzhen.app.ui.RU_LOCALE
import com.sterzhen.app.ui.theme.Ink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private fun fallbackState(p: Principle) = PrincipleState(
    principle = p,
    currentStreak = 0,
    bestStreak = 0,
    slippedToday = false,
    tracked = false,
    lastDays = List(14) { null }
)

/**
 * «Стержень»: цилиндрическая 3D-карусель из 7 карточек.
 * Карточки расставлены по кругу радиусом R, повёрнуты по нормали к центру
 * (rotationY), глубиной масштабируются перспективой и гаснут к задней стенке.
 */
@Composable
fun CoreScreen(vm: AppViewModel) {
    val states by vm.principleStates.collectAsState()
    val stateById = states.associateBy { it.principle.id }

    var angle by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var animating by remember { mutableStateOf(false) }
    var autoRotate by remember { mutableStateOf(true) }
    var showStartPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val count = Principles.ALL.size
    val step = 360f / count

    fun settleTo(target: Float) {
        scope.launch {
            animating = true
            animate(
                initialValue = angle,
                targetValue = target,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 260f)
            ) { value, _ -> angle = value }
            animating = false
        }
    }

    val frontIndex by remember {
        derivedStateOf {
            Principles.ALL.indices.minByOrNull { i ->
                val raw = (angle + i * step) % 360f
                val normalized = ((raw + 540f) % 360f) - 180f
                abs(normalized)
            } ?: 0
        }
    }

    LaunchedEffect(autoRotate, dragging, animating) {
        if (autoRotate && !dragging && !animating) {
            while (true) {
                delay(16)
                angle += 0.14f
            }
        }
    }

    // Медленный автоскролл до первого касания пользователя.
    val stopAuto = { autoRotate = false }

    val frontPrinciple = Principles.ALL[frontIndex]
    val frontState = stateById[frontPrinciple.id] ?: fallbackState(frontPrinciple)
    val accent = Color(frontPrinciple.colorHex)
    val density = LocalDensity.current.density

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionHeader(title = "Стержень")
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "7 принципов",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Pill(text = "${frontState.currentStreak} дн.", color = accent)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Крути карусель · нажми на карточку, чтобы вынести её вперёд",
            style = MaterialTheme.typography.bodySmall,
            color = Ink.TextFaint
        )

        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(392.dp)
                .clipToBounds()
                .draggable(
                    state = rememberDraggableState { delta ->
                        stopAuto()
                        angle += delta * 0.22f
                    },
                    orientation = Orientation.Horizontal,
                    onDragStarted = { dragging = true; stopAuto() },
                    onDragStopped = {
                        dragging = false
                        settleTo((angle / step).roundToInt() * step)
                    }
                )
        ) {
            val width = maxWidth
            val height = maxHeight
            val cx = width / 2
            val cy = height / 2
            val radius = width * 0.50f
            val cardW = 158.dp
            val cardH = 202.dp

            // Тёплое свечение под передней карточкой.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension * 0.72f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.62f),
                        radius = r
                    ),
                    radius = r,
                    center = Offset(size.width / 2f, size.height * 0.62f)
                )
            }

            val ordered = Principles.ALL.indices.sortedBy { i ->
                cos((angle + i * step) * PI / 180.0)
            }

            ordered.forEach { index ->
                val principle = Principles.ALL[index]
                val st = stateById[principle.id] ?: fallbackState(principle)
                val a = angle + index * step
                val rad = a * PI / 180.0
                val cosA = cos(rad).toFloat()
                val sinA = sin(rad).toFloat()
                val t = (cosA + 1f) / 2f
                val perspective = 0.78f + 0.22f * t
                val cardColor = Color(principle.colorHex)

                Box(
                    modifier = Modifier
                        .offset(
                            x = cx + radius * sinA - cardW / 2,
                            y = cy - 30.dp * (1f - t) - cardH / 2
                        )
                        .size(cardW, cardH)
                        .zIndex(cosA)
                        .graphicsLayer {
                            rotationY = a % 360f
                            cameraDistance = 16f * density
                            scaleX = perspective
                            scaleY = perspective
                            alpha = 0.18f + 0.82f * t
                        }
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    cardColor.copy(alpha = 0.95f),
                                    cardColor.copy(alpha = 0.45f)
                                )
                            )
                        )
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                            )
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.10f + 0.32f * t),
                            RoundedCornerShape(26.dp)
                        )
                        .clickable(enabled = cosA > 0.2f) {
                            stopAuto()
                            val current = ((angle + index * step) % 360f + 540f) % 360f - 180f
                            settleTo(angle - current)
                        }
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = principle.emoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = principle.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (st.slippedToday) {
                            Text(
                                text = "СРЫВ",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD7DB),
                                letterSpacing = 1.4.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (st.currentStreak > 0) "🔥" else "🧊",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${st.currentStreak}",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Индикаторы позиции.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Principles.ALL.forEachIndexed { i, p ->
                val active = i == frontIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) Color(p.colorHex)
                            else Color.White.copy(alpha = 0.16f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Детали активного принципа.
        GlassCard(modifier = Modifier.fillMaxWidth(), glow = accent) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StreakRing(
                    streak = frontState.currentStreak,
                    color = accent,
                    emoji = frontPrinciple.emoji,
                    dimension = 54.dp,
                    strokeWidth = 3.5.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = frontPrinciple.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = frontPrinciple.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.TextDim
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    BigStreak(streak = frontState.currentStreak, fontSize = 40, color = accent)
                    Text(
                        text = "дней",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.TextFaint
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(text = "рекорд ${frontState.bestStreak}", color = Ink.Gold)
                Pill(
                    text = if (frontState.currentStreak >= 30) "легенда" else "до 30 дней: ${
                        (30 - frontState.currentStreak).coerceAtLeast(0)
                    }",
                    color = Ink.TextDim
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            MilestoneBar(streak = frontState.currentStreak, color = accent)

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Последние 14 дней",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.TextFaint
                )
                DayStrip(days = frontState.lastDays)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .clickable { showStartPicker = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Начало отсчёта",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.TextFaint
                    )
                    Text(
                        text = frontState.startDate
                            ?.format(DateTimeFormatter.ofPattern("d MMMM yyyy", RU_LOCALE))
                            ?: "не задано",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink.Text
                    )
                }
                Text(
                    text = "изменить",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.Gold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            if (frontState.slippedToday) {
                OutlineButton(
                    text = "Отменить срыв",
                    modifier = Modifier.fillMaxWidth(),
                    color = Ink.Mint
                ) {
                    vm.toggleSlip(frontPrinciple.id, true)
                }
            } else {
                OutlineButton(
                    text = "Сообщить о срыве",
                    modifier = Modifier.fillMaxWidth(),
                    color = Ink.Danger
                ) {
                    vm.toggleSlip(frontPrinciple.id, false)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Держишься — просто живи. Огонёк растёт сам.",
                style = MaterialTheme.typography.bodySmall,
                color = Ink.TextFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.9f)
            )
        }
        Spacer(modifier = Modifier.height(120.dp))
    }

    if (showStartPicker) {
        StartDateDialog(
            title = "С какой даты считать «${frontPrinciple.title}»",
            initial = frontState.startDate,
            onDismiss = { showStartPicker = false },
            onConfirm = { date ->
                vm.setStartDate(frontPrinciple.id, date)
                showStartPicker = false
            }
        )
    }
}

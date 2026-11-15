package com.sterzhen.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.sterzhen.app.data.Principles
import com.sterzhen.app.ui.AppViewModel
import com.sterzhen.app.ui.PrincipleState
import com.sterzhen.app.ui.components.DaysDots
import com.sterzhen.app.ui.components.FireStreak
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Экран «Стержень»: круговая 3D-карусель из 7 принципов.
 * Карточки стоят по кругу, поворачиваются в 3D (rotationY + перспектива),
 * крутятся свайпом и сами медленно вращаются, когда не трогаешь.
 * Карточка спереди — активная: под каруселью её детали и кнопка отметки.
 */
@Composable
fun CoreScreen(vm: AppViewModel) {
    val states by vm.principleStates.collectAsState()
    val stateById = states.associateBy { it.principle.id }

    var angle by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var isSnapping by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val count = Principles.ALL.size
    val step = 360f / count

    // Автовращение, когда карусель не трогают и не доводят до карточки.
    LaunchedEffect(isDragging, isSnapping) {
        if (!isDragging && !isSnapping) {
            while (true) {
                angle += 0.12f
                delay(16)
            }
        }
    }

    fun animateAngleTo(target: Float, durationMs: Long = 400L) {
        isSnapping = true
        scope.launch {
            val start = angle
            val startTime = withFrameMillis { it }
            while (true) {
                val now = withFrameMillis { it }
                val progress = ((now - startTime) / durationMs.toFloat()).coerceIn(0f, 1f)
                val eased = 1f - (1f - progress) * (1f - progress)
                angle = start + (target - start) * eased
                if (progress >= 1f) break
            }
            isSnapping = false
        }
    }

    fun snapToNearest() {
        animateAngleTo((angle / step).roundToInt() * step)
    }

    fun bringToFront(index: Int) {
        val current = ((angle + index * step) % 360f + 360f) % 360f
        val delta = if (current > 180f) 360f - current else -current
        animateAngleTo(angle + delta, durationMs = 450L)
    }

    // Какая карточка сейчас спереди (ближе всего к углу 0).
    val frontIndex by remember {
        derivedStateOf {
            Principles.ALL.indices.minByOrNull { i ->
                val a = ((angle + i * step) % 360f + 360f) % 360f
                minOf(a, 360f - a)
            } ?: 0
        }
    }
    val frontState = stateById[Principles.ALL[frontIndex].id]
        ?: PrincipleState(
            principle = Principles.ALL[frontIndex],
            currentStreak = 0,
            bestStreak = 0,
            keptToday = false,
            lastDays = List(14) { null }
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Стержень",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Тяни, чтобы крутить. Нажми на карточку, чтобы поднести её вперёд.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .draggable(
                    state = rememberDraggableState { delta -> angle += delta * 0.35f },
                    orientation = Orientation.Horizontal,
                    onDragStarted = { isDragging = true },
                    onDragStopped = {
                        isDragging = false
                        snapToNearest()
                    }
                )
        ) {
            val cx = maxWidth / 2
            val cy = maxHeight / 2
            val radiusX = maxWidth * 0.36f
            val radiusY = maxHeight * 0.30f
            val cardW = 132.dp
            val cardH = 176.dp

            Principles.ALL.forEachIndexed { index, principle ->
                val st = stateById[principle.id]
                    ?: PrincipleState(
                        principle = principle,
                        currentStreak = 0,
                        bestStreak = 0,
                        keptToday = false,
                        lastDays = List(14) { null }
                    )
                val total = angle + index * step
                val rad = total * PI / 180f
                val x = cx + radiusX * sin(rad).toFloat()
                val y = cy + radiusY * cos(rad).toFloat()
                val depth = cos(rad).toFloat() // 1 — спереди (снизу), -1 — сзади (сверху)
                val t = (depth + 1f) / 2f
                val scale = 0.7f + 0.3f * t
                val alpha = 0.35f + 0.65f * t
                val baseColor = Color(principle.colorHex)

                Box(
                    modifier = Modifier
                        .offset(x = x - cardW / 2, y = y - cardH / 2)
                        .size(cardW, cardH)
                        .zIndex(depth)
                        .graphicsLayer {
                            rotationY = total % 360f
                            cameraDistance = 1000f
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    baseColor,
                                    baseColor.copy(alpha = 0.55f)
                                )
                            )
                        )
                        .clickable(enabled = depth > 0f) { bringToFront(index) }
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = principle.emoji, fontSize = 38.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = principle.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (st.currentStreak > 0) "🔥 ${st.currentStreak}" else "🧊 0",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "лучший: ${st.bestStreak}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = frontState.principle.emoji, fontSize = 34.sp)
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = frontState.principle.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = frontState.principle.subtitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                    FireStreak(streak = frontState.currentStreak)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Лучший стрик: ${frontState.bestStreak} дн.",
                        fontSize = 14.sp
                    )
                    DaysDots(days = frontState.lastDays)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { vm.toggleKept(frontState.principle.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (frontState.keptToday) {
                            "✓ Держу сегодня — отменить"
                        } else {
                            "🔥 Держу сегодня"
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

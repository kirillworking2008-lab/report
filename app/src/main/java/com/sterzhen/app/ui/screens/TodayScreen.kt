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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.sterzhen.app.data.DateUtils
import com.sterzhen.app.data.Principle
import com.sterzhen.app.data.Principles
import com.sterzhen.app.data.db.TaskEntity
import com.sterzhen.app.ui.AppViewModel
import com.sterzhen.app.ui.PrincipleState
import com.sterzhen.app.ui.RU_LOCALE
import com.sterzhen.app.ui.components.BigStreak
import com.sterzhen.app.ui.components.DayStrip
import com.sterzhen.app.ui.components.FlameBurst
import com.sterzhen.app.ui.components.GlassCard
import com.sterzhen.app.ui.components.MilestoneBar
import com.sterzhen.app.ui.components.OutlineButton
import com.sterzhen.app.ui.components.Pill
import com.sterzhen.app.ui.components.SectionHeader
import com.sterzhen.app.ui.components.StartDateDialog
import com.sterzhen.app.ui.components.StreakRing
import com.sterzhen.app.ui.theme.Ink
import com.sterzhen.app.ui.theme.Radii
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
 * «Сегодня»: стержень (3D-карусель принципов + детали активного) и задачи дня.
 */
@Composable
fun TodayScreen(vm: AppViewModel) {
    val today by vm.today.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val states by vm.principleStates.collectAsState()
    val heldCount by vm.heldTodayCount.collectAsState()

    var newTask by remember { mutableStateOf("") }
    var showStartPicker by remember { mutableStateOf(false) }

    var angle by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var animating by remember { mutableStateOf(false) }
    var autoRotate by remember { mutableStateOf(true) }
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
                abs(((raw + 540f) % 360f) - 180f)
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

    val stopAuto = { autoRotate = false }
    val stateById = states.associateBy { it.principle.id }
    val frontPrinciple = Principles.ALL[frontIndex]
    val frontState = stateById[frontPrinciple.id] ?: fallbackState(frontPrinciple)
    val accent = Ink.Ember
    val density = LocalDensity.current.density

    val todayStr = DateUtils.format(today)
    val todayTasks = tasks.filter { it.date == todayStr }
    val doneCount = todayTasks.count { it.done }
    val maxStreak = states.maxOfOrNull { it.currentStreak } ?: 0
    val slipsToday = states.count { it.slippedToday }
    val dateLabel = today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", RU_LOCALE))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 130.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateLabel.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink.TextFaint
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Сегодня",
                        style = MaterialTheme.typography.displayMedium,
                        color = Ink.Ember
                    )
                }
                FlameBurst(streak = maxStreak, dimension = 62.dp)
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), glow = Ink.Ember) {
                Row(verticalAlignment = Alignment.Bottom) {
                    BigStreak(streak = maxStreak, fontSize = 46)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "дней",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink.TextDim,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = "Лучший огонёк из 7 принципов",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.TextDim
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStat(value = "$heldCount/${states.size}", label = "принципов", color = Ink.Mint)
                    MiniStat(value = "$doneCount/${todayTasks.size}", label = "задач", color = Ink.Gold)
                    MiniStat(
                        value = "$slipsToday",
                        label = "срывов",
                        color = if (slipsToday > 0) Ink.Danger else Ink.TextDim
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Стержень",
                    caption = "Крути карусель · нажми на карточку, чтобы вынести её вперёд",
                    modifier = Modifier.weight(1f)
                )
                Pill(text = "🔥 ${frontState.currentStreak}", color = accent)
            }
        }

        // 3D-карусель принципов.
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clipToBounds()
                    .draggable(
                        state = rememberDraggableState { delta ->
                            stopAuto()
                            angle += delta * 0.22f
                        },
                        orientation = Orientation.Horizontal,
                        onDragStarted = {
                            dragging = true
                            stopAuto()
                        },
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
                val radius = width * 0.54f
                val cardW = 150.dp
                val cardH = 196.dp
                val cardShape = RoundedCornerShape(Radii.Hero)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = size.minDimension * 0.72f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.20f), Color.Transparent),
                            center = Offset(size.width / 2f, size.height * 0.6f),
                            radius = r
                        ),
                        radius = r,
                        center = Offset(size.width / 2f, size.height * 0.6f)
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
                    val perspective = 0.74f + 0.26f * t
                    val cardColor = Color(principle.colorHex)

                    Box(
                        modifier = Modifier
                            .offset(
                                x = cx + radius * sinA - cardW / 2,
                                y = cy - 34.dp * (1f - t) - cardH / 2
                            )
                            .size(cardW, cardH)
                            .zIndex(cosA)
                            .shadow(
                                elevation = (5 + 16 * t).dp,
                                shape = cardShape,
                                clip = false
                            )
                            .graphicsLayer {
                                rotationY = a % 360f
                                rotationX = -9f
                                cameraDistance = 12f * density
                                scaleX = perspective
                                scaleY = perspective
                                alpha = 0.16f + 0.84f * t
                            }
                            .clip(cardShape)
                            // тёмная стеклянная база кита
                            .background(
                                Brush.verticalGradient(listOf(Ink.CardHi, Ink.Card))
                            )
                            // оттенок принципа — только как точечное свечение
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        cardColor.copy(alpha = 0.28f * t + 0.10f),
                                        Color.Transparent
                                    ),
                                    center = Offset(58f, 26f),
                                    radius = 270f
                                )
                            )
                            // слой стекла 145deg
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.055f),
                                        Color.White.copy(alpha = 0.022f)
                                    )
                                )
                            )
                            // мягкое затухание вниз
                            .background(
                                Brush.verticalGradient(
                                    0.42f to Color.Transparent,
                                    0.78f to Color.Black.copy(alpha = 0.22f),
                                    1f to Color.Black.copy(alpha = 0.42f)
                                )
                            )
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.18f * t + 0.06f),
                                        Color.White.copy(alpha = 0.03f)
                                    )
                                ),
                                cardShape
                            )
                            .clickable(enabled = cosA > 0.2f) {
                                stopAuto()
                                val current = ((angle + index * step) % 360f + 540f) % 360f - 180f
                                settleTo(angle - current)
                            }
                            .padding(13.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.10f))
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = principle.emoji, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.height(9.dp))
                            Text(
                                text = principle.title,
                                fontWeight = FontWeight(680),
                                fontSize = 13.sp,
                                letterSpacing = (-0.3).sp,
                                color = Ink.Text,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (st.slippedToday) {
                                Text(
                                    text = "СРЫВ",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink.Danger,
                                    letterSpacing = 1.4.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (st.currentStreak > 0) "🔥" else "🧊",
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${st.currentStreak}",
                                    fontWeight = FontWeight(680),
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.5).sp,
                                    color = Ink.Text
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Principles.ALL.forEachIndexed { i, _ ->
                    val active = i == frontIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) Ink.Ember else Color.White.copy(alpha = 0.16f)
                            )
                    )
                }
            }
        }

        // Детали активного принципа.
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), glow = accent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StreakRing(
                        streak = frontState.currentStreak,
                        color = accent,
                        emoji = frontPrinciple.emoji,
                        dimension = 52.dp,
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
                        BigStreak(streak = frontState.currentStreak, fontSize = 36, color = accent)
                        Text(
                            text = "дней",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink.TextFaint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(text = "рекорд ${frontState.bestStreak}", color = Ink.Gold)
                    Pill(
                        text = if (frontState.currentStreak >= 30) "легенда"
                        else "до 30: ${(30 - frontState.currentStreak).coerceAtLeast(0)}",
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

                Spacer(modifier = Modifier.height(14.dp))
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
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            SectionHeader(title = "Задачи на сегодня")
        }

        if (todayTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radii.Card))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(Radii.Card))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Пусто. Добавь первую задачу — и день начнётся.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink.TextDim
                    )
                }
            }
        }

        items(todayTasks, key = { it.id }) { task ->
            TaskRow(
                task = task,
                onToggle = { vm.toggleTask(task) },
                onDelete = { vm.deleteTask(task) }
            )
        }

        item {
            AddTaskField(
                value = newTask,
                onValueChange = { newTask = it },
                onAdd = {
                    vm.addTask(newTask, today)
                    newTask = ""
                }
            )
        }
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

@Composable
private fun MiniStat(value: String, label: String, color: Color) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Ink.TextFaint
        )
    }
}

@Composable
private fun TaskRow(task: TaskEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Card))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(Radii.Card))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (task.done) Ink.Mint.copy(alpha = 0.9f) else Color.Transparent)
                .border(
                    1.5.dp,
                    if (task.done) Ink.Mint else Color.White.copy(alpha = 0.25f),
                    CircleShape
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (task.done) {
                Text(text = "✓", fontSize = 13.sp, color = Color(0xFF101716))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (task.done) Ink.TextFaint else Ink.Text,
            textDecoration = if (task.done) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✕", fontSize = 13.sp, color = Ink.TextFaint)
        }
    }
}

@Composable
private fun AddTaskField(value: String, onValueChange: (String) -> Unit, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Card))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(Radii.Card))
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = "Новая задача…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink.TextFaint
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink.Text),
                cursorBrush = Brush.horizontalGradient(listOf(Ink.Ember, Ink.Gold)),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(Ink.Ember, Ink.EmberDeep)))
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101716)
            )
        }
    }
}

package com.sterzhen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sterzhen.app.data.DateUtils
import com.sterzhen.app.data.db.TaskEntity
import com.sterzhen.app.ui.AppViewModel
import com.sterzhen.app.ui.PrincipleState
import com.sterzhen.app.ui.RU_LOCALE
import com.sterzhen.app.ui.components.BigStreak
import com.sterzhen.app.ui.components.DayStrip
import com.sterzhen.app.ui.components.FlameBurst
import com.sterzhen.app.ui.components.GlassCard
import com.sterzhen.app.ui.components.GlowButton
import com.sterzhen.app.ui.components.OutlineButton
import com.sterzhen.app.ui.components.Pill
import com.sterzhen.app.ui.components.SectionHeader
import com.sterzhen.app.ui.components.StreakRing
import com.sterzhen.app.ui.theme.Ink
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(vm: AppViewModel) {
    val today by vm.today.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val states by vm.principleStates.collectAsState()
    val heldCount by vm.heldTodayCount.collectAsState()

    var newTask by remember { mutableStateOf("") }
    var sheetState by remember { mutableStateOf<PrincipleState?>(null) }

    val todayStr = DateUtils.format(today)
    val todayTasks = tasks.filter { it.date == todayStr }
    val doneCount = todayTasks.count { it.done }
    val maxStreak = states.maxOfOrNull { it.currentStreak } ?: 0
    val slipsToday = states.count { it.slippedToday }
    val dateLabel = today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", RU_LOCALE))

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 20.dp,
                bottom = 130.dp
            )
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
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                    FlameBurst(streak = maxStreak, dimension = 64.dp)
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), glow = Ink.Ember) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ЛУЧШИЙ ОГОНЁК",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ink.TextFaint
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                BigStreak(streak = maxStreak, fontSize = 56)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "дней",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Ink.TextDim,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                            Text(
                                text = "Без срывов по лучшему принципу",
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink.TextDim
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniStat(
                            value = "$heldCount/${states.size}",
                            label = "принципов",
                            color = Ink.Mint
                        )
                        MiniStat(
                            value = "$doneCount/${todayTasks.size}",
                            label = "задач",
                            color = Ink.Gold
                        )
                        MiniStat(
                            value = "$slipsToday",
                            label = "срывов",
                            color = if (slipsToday > 0) Ink.Danger else Ink.TextDim
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    title = "Стержень",
                    caption = "Дни идут сами — отмечай только срывы"
                )
            }

            items(states, key = { it.principle.id }) { st ->
                PrincipleRow(state = st) { sheetState = st }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(title = "Задачи на сегодня")
            }

            if (todayTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(24.dp)
                            )
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
                Spacer(modifier = Modifier.height(4.dp))
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

        val sheet = sheetState
        if (sheet != null) {
            val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { sheetState = null },
                sheetState = state,
                containerColor = Ink.Card
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StreakRing(
                            streak = sheet.currentStreak,
                            color = Color(sheet.principle.colorHex),
                            emoji = sheet.principle.emoji,
                            dimension = 52.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sheet.principle.title,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "${sheet.currentStreak} дн. подряд · рекорд ${sheet.bestStreak}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink.TextDim
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Последние 14 дней",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink.TextFaint
                        )
                        DayStrip(days = sheet.lastDays, barHeight = 24.dp)
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                    if (sheet.slippedToday) {
                        OutlineButton(
                            text = "Отменить срыв сегодня",
                            modifier = Modifier.fillMaxWidth(),
                            color = Ink.Mint
                        ) {
                            vm.toggleSlip(sheet.principle.id, true)
                            sheetState = null
                        }
                    } else {
                        GlowButton(
                            text = "Сорвался сегодня",
                            modifier = Modifier.fillMaxWidth(),
                            colors = listOf(Ink.Danger, Color(0xFFC22B4A))
                        ) {
                            vm.toggleSlip(sheet.principle.id, false)
                            sheetState = null
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
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
private fun PrincipleRow(state: PrincipleState, onClick: () -> Unit) {
    val accent = Color(state.principle.colorHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accent.copy(alpha = if (state.slippedToday) 0.16f else 0.10f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StreakRing(
            streak = state.currentStreak,
            color = accent,
            emoji = state.principle.emoji,
            dimension = 44.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.principle.title,
                style = MaterialTheme.typography.titleMedium,
                color = Ink.Text
            )
            Text(
                text = if (state.slippedToday) "Срыв отмечен" else state.principle.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (state.slippedToday) Ink.Danger else Ink.TextFaint
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Pill(
            text = if (state.currentStreak > 0) "🔥 ${state.currentStreak}" else "—",
            color = if (state.currentStreak > 0) accent else Ink.TextFaint
        )
    }
}

@Composable
private fun TaskRow(task: TaskEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (task.done) Ink.Mint.copy(alpha = 0.9f) else Color.Transparent
                )
                .border(
                    1.5.dp,
                    if (task.done) Ink.Mint else Color.White.copy(alpha = 0.25f),
                    CircleShape
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (task.done) {
                Text(text = "✓", fontSize = 13.sp, color = Color(0xFF06120C))
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
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
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
                color = Color(0xFF190A02)
            )
        }
    }
}

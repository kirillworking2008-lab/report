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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.sterzhen.app.ui.RU_LOCALE
import com.sterzhen.app.ui.components.GlassCard
import com.sterzhen.app.ui.components.Pill
import com.sterzhen.app.ui.components.SectionHeader
import com.sterzhen.app.ui.components.TemplateItemsDialog
import com.sterzhen.app.ui.components.TemplateNameDialog
import com.sterzhen.app.ui.components.TemplatePickerDialog
import com.sterzhen.app.ui.dayShortName
import com.sterzhen.app.ui.theme.Ink
import com.sterzhen.app.ui.theme.Radii
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun WeekScreen(vm: AppViewModel) {
    val today by vm.today.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val slips by vm.slips.collectAsState()

    var weekOffset by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf(today) }
    var newTask by remember { mutableStateOf("") }
    var showTemplates by remember { mutableStateOf(false) }
    var showSaveTemplate by remember { mutableStateOf(false) }
    var showNewTemplate by remember { mutableStateOf(false) }
    val templates by vm.templates.collectAsState()

    val monday = today
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .plusWeeks(weekOffset.toLong())
    val days: List<LocalDate> = (0..6).map { monday.plusDays(it.toLong()) }
    if (selected !in days) selected = if (today in days) today else days.first()

    val tasksByDate = tasks.groupBy { it.date }
    val slipsByDate = slips.groupBy { it.date }

    val weekTasks = days.flatMap { tasksByDate[DateUtils.format(it)].orEmpty() }
    val weekDone = weekTasks.count { it.done }
    val weekSlips = days.sumOf { slipsByDate[DateUtils.format(it)]?.size ?: 0 }
    val cleanDays = days.count { day ->
        DateUtils.format(day) <= DateUtils.format(today) &&
            slipsByDate[DateUtils.format(day)].isNullOrEmpty()
    }
    val selectedKey = DateUtils.format(selected)
    val selectedTasks = tasksByDate[selectedKey].orEmpty()
    val selectedSlips = slipsByDate[selectedKey].orEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 20.dp, bottom = 130.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionHeader(title = "Неделя")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${monday.format(DateTimeFormatter.ofPattern("d MMM", RU_LOCALE))} — " +
                            monday.plusDays(6).format(DateTimeFormatter.ofPattern("d MMMM", RU_LOCALE)),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink.Ember
                    )
                }
                RoundIcon(text = "‹") { weekOffset -= 1 }
                Spacer(modifier = Modifier.width(8.dp))
                RoundIcon(text = "›") { weekOffset += 1 }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                days.forEach { day ->
                    val key = DateUtils.format(day)
                    val dayDone = tasksByDate[key].orEmpty().count { it.done }
                    val dayTasks = tasksByDate[key].orEmpty().size
                    val daySlips = slipsByDate[key]?.size ?: 0
                    val isSelected = day == selected
                    val isToday = day == today
                    val accent = if (isSelected) {
                        listOf(Ink.Ember, Ink.EmberDeep)
                    } else {
                        listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.03f))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Brush.verticalGradient(accent))
                            .border(
                                1.dp,
                                if (isToday && !isSelected) Ink.Ember.copy(alpha = 0.45f)
                                else Color.White.copy(alpha = 0.07f),
                                RoundedCornerShape(13.dp)
                            )
                            .clickable { selected = day }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayShortName(day.dayOfWeek).take(2),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Color(0xFF101716) else Ink.TextFaint
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${day.dayOfMonth}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF101716) else Ink.Text
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        daySlips > 0 -> Ink.Danger
                                        dayTasks > 0 && dayDone == dayTasks -> Ink.Mint
                                        dayTasks > 0 -> Ink.Gold.copy(alpha = 0.8f)
                                        else -> Color.White.copy(alpha = 0.15f)
                                    }
                                )
                        )
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BigStat(value = "$weekDone", label = "выполнено", color = Ink.Text)
                    BigStat(value = "$weekSlips", label = "срывов", color = if (weekSlips > 0) Ink.Danger else Ink.TextDim)
                    BigStat(value = "$cleanDays", label = "чистых дней", color = Ink.Mint)
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selected.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", RU_LOCALE))
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Pill(
                    text = if (selectedSlips.isEmpty()) "без срывов" else "срывов: ${selectedSlips.size}",
                    color = if (selectedSlips.isEmpty()) Ink.Mint else Ink.Danger
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Ink.Ember.copy(alpha = 0.10f))
                        .border(1.dp, Ink.Ember.copy(alpha = 0.30f), CircleShape)
                        .clickable { showTemplates = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "📋 шаблон",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight(680),
                        color = Ink.Ember
                    )
                }
            }
        }

        if (selectedTasks.isEmpty()) {
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
                        text = "На этот день задач нет.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink.TextDim
                    )
                }
            }
        }

        items(selectedTasks, key = { it.id }) { task ->
            WeekTaskRow(
                task = task,
                onToggle = { vm.toggleTask(task) },
                onDelete = { vm.deleteTask(task) }
            )
        }

        item {
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
                    if (newTask.isEmpty()) {
                        Text(
                            text = "Задача на этот день…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Ink.TextFaint
                        )
                    }
                    BasicTextField(
                        value = newTask,
                        onValueChange = { newTask = it },
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
                        .clickable {
                            vm.addTask(newTask, selected)
                            newTask = ""
                        },
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
    }

    if (showTemplates) {
        TemplatePickerDialog(
            templates = templates,
            dayLabel = selected.format(DateTimeFormatter.ofPattern("d MMMM", RU_LOCALE)),
            dayTaskCount = selectedTasks.size,
            onPick = { template ->
                vm.applyTemplate(template.id, selected)
                showTemplates = false
            },
            onSaveDayAsTemplate = {
                showTemplates = false
                if (selectedTasks.isNotEmpty()) showSaveTemplate = true else showNewTemplate = true
            },
            onDismiss = { showTemplates = false }
        )
    }

    if (showSaveTemplate) {
        TemplateNameDialog(
            title = "Шаблон из задач дня",
            initial = "",
            hint = "Сохраним ${selectedTasks.size} задач(и) выбранного дня",
            onDismiss = { showSaveTemplate = false },
            onConfirm = { name ->
                vm.createTemplate(name, selectedTasks.map { it.title })
                showSaveTemplate = false
            }
        )
    }

    if (showNewTemplate) {
        TemplateItemsDialog(
            onDismiss = { showNewTemplate = false },
            onConfirm = { name, items ->
                vm.createTemplate(name, items)
                showNewTemplate = false
            }
        )
    }
}

@Composable
private fun RoundIcon(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.09f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Ink.Text
        )
    }
}

@Composable
private fun BigStat(value: String, label: String, color: Color) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
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
private fun WeekTaskRow(task: TaskEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
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
            if (task.done) Text(text = "✓", fontSize = 13.sp, color = Color(0xFF101716))
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

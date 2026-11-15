package com.sterzhen.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sterzhen.app.data.DateUtils
import com.sterzhen.app.data.db.TaskEntity
import com.sterzhen.app.ui.AppViewModel
import com.sterzhen.app.ui.RU_LOCALE
import com.sterzhen.app.ui.dayShortName
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun WeekScreen(vm: AppViewModel) {
    val today by vm.today.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val slips by vm.slips.collectAsState()
    var newTask by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(today) }

    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days: List<LocalDate> = (0..6).map { monday.plusDays(it.toLong()) }
    if (selected !in days) selected = today

    val tasksByDate = tasks.groupBy { it.date }
    val slipsByDate = slips.groupBy { it.date }

    val weekTasks = days.flatMap { tasksByDate[DateUtils.format(it)].orEmpty() }
    val weekDone = weekTasks.count { it.done }
    val weekSlips = days.sumOf { slipsByDate[DateUtils.format(it)]?.size ?: 0 }
    val idealDays = days.count {
        DateUtils.format(it) <= DateUtils.format(today) &&
            (slipsByDate[DateUtils.format(it)]?.isEmpty() ?: true)
    }

    val selectedTasks = tasksByDate[DateUtils.format(selected)].orEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Неделя",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${monday.format(DateTimeFormatter.ofPattern("d MMMM", RU_LOCALE))} — " +
                    "${monday.plusDays(6).format(DateTimeFormatter.ofPattern("d MMMM", RU_LOCALE))}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeekStat(value = "$weekDone/${weekTasks.size}", label = "задач выполнено")
                    WeekStat(value = "$weekSlips", label = "срывов")
                    WeekStat(value = "$idealDays", label = "чистых дней")
                }
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
                    val daySlips = slipsByDate[key]?.size ?: 0
                    val isSelected = day == selected
                    val isToday = day == today
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable { selected = day }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayShortName(day.dayOfWeek),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${day.dayOfMonth}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "✓$dayDone",
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (daySlips == 0) "🔥" else "⚠$daySlips",
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = selected.format(
                    DateTimeFormatter.ofPattern("EEEE, d MMMM", RU_LOCALE)
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (selectedTasks.isEmpty()) {
            item {
                Text(
                    text = "На этот день задач нет.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newTask,
                    onValueChange = { newTask = it },
                    label = { Text("Задача на этот день") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        vm.addTask(newTask, selected)
                        newTask = ""
                    }
                ) {
                    Text("+")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WeekStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WeekTaskRow(task: TaskEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Text(
                text = task.title,
                modifier = Modifier.weight(1f),
                textDecoration = if (task.done) TextDecoration.LineThrough else null,
                color = if (task.done) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            IconButton(onClick = onDelete) {
                Text("🗑")
            }
        }
    }
}

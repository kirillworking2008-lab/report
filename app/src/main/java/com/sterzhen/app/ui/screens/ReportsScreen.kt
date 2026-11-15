package com.sterzhen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sterzhen.app.data.DateUtils
import com.sterzhen.app.ui.AppViewModel
import com.sterzhen.app.ui.PrincipleState
import com.sterzhen.app.ui.components.BigStreak
import com.sterzhen.app.ui.components.DayStrip
import com.sterzhen.app.ui.components.GlassCard
import com.sterzhen.app.ui.components.Pill
import com.sterzhen.app.ui.components.SectionHeader
import com.sterzhen.app.ui.components.StreakRing
import com.sterzhen.app.ui.dayShortName
import com.sterzhen.app.ui.theme.Ink

@Composable
fun ReportsScreen(vm: AppViewModel) {
    val today by vm.today.collectAsState()
    val tasks by vm.tasks.collectAsState()
    val slips by vm.slips.collectAsState()
    val states by vm.principleStates.collectAsState()

    val totalDone = tasks.count { it.done }
    val totalTasks = tasks.size
    val totalSlips = slips.size
    val bestOverall = states.maxOfOrNull { it.bestStreak } ?: 0
    val currentBest = states.maxOfOrNull { it.currentStreak } ?: 0

    val last7 = (6 downTo 0).map { offset ->
        val day = today.minusDays(offset.toLong())
        val done = tasks.count { it.date == DateUtils.format(day) && it.done }
        day to done
    }
    val peak = last7.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 20.dp, bottom = 130.dp)
    ) {
        item {
            SectionHeader(title = "Отчёты")
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Твоя динамика",
                style = MaterialTheme.typography.displayMedium
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "ОГОНЁК",
                    value = "$currentBest",
                    unit = "дней",
                    color = Ink.Ember
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "РЕКОРД",
                    value = "$bestOverall",
                    unit = "дней",
                    color = Ink.Gold
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "ЗАДАЧИ",
                    value = "$totalDone",
                    unit = "из $totalTasks",
                    color = Ink.Mint
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "СРЫВЫ",
                    value = "$totalSlips",
                    unit = "всего",
                    color = if (totalSlips > 0) Ink.Danger else Ink.TextDim
                )
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ЗАДАЧИ ЗА 7 ДНЕЙ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink.TextFaint,
                        modifier = Modifier.weight(1f)
                    )
                    Pill(text = "пик $peak", color = Ink.Gold)
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    last7.forEach { (day, done) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = "$done",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (done > 0) Ink.Text else Ink.TextFaint
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .height(
                                        (14 + (done.toFloat() / peak) * 78f).dp
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (day == today) {
                                            Brush.verticalGradient(listOf(Ink.Ember, Ink.EmberDeep))
                                        } else {
                                            Brush.verticalGradient(
                                                listOf(
                                                    Ink.Ember.copy(alpha = 0.55f),
                                                    Ink.EmberDeep.copy(alpha = 0.25f)
                                                )
                                            )
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dayShortName(day.dayOfWeek).take(2),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (day == today) Ink.Text else Ink.TextFaint
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(
                title = "Стержень по принципам",
                caption = "Полоски — последние 14 дней"
            )
        }

        items(states, key = { it.principle.id }) { st ->
            PrincipleReportRow(state = st)
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    color: Color
) {
    GlassCard(modifier = modifier, shape = RoundedCornerShape(24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Ink.TextFaint
        )
        Spacer(modifier = Modifier.height(10.dp))
        BigStreak(streak = value.toIntOrNull() ?: 0, fontSize = 34, color = color)
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = Ink.TextDim
        )
    }
}

@Composable
private fun PrincipleReportRow(state: PrincipleState) {
    val accent = Color(state.principle.colorHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
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
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            DayStrip(days = state.lastDays, barHeight = 16.dp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${state.currentStreak}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = accent
            )
            Text(
                text = "рекорд ${state.bestStreak}",
                fontSize = 10.sp,
                color = Ink.TextFaint
            )
        }
    }
}

package com.sterzhen.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Огонёк стрика: 🔥 + число дней. Цвет зависит от длины серии. */
@Composable
fun FireStreak(streak: Int, modifier: Modifier = Modifier) {
    val fireColor = when {
        streak >= 30 -> Color(0xFFFF3D00)
        streak >= 7 -> Color(0xFFFF6D00)
        streak >= 1 -> Color(0xFFFF9100)
        else -> Color.Gray
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (streak > 0) "🔥" else "🧊",
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$streak",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = fireColor
        )
    }
}

/** Точки истории за последние дни: зелёная — держался, серая — не отмечено. */
@Composable
fun DaysDots(days: List<Boolean?>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        when (day) {
                            true -> Color(0xFF66BB6A)
                            false -> Color(0xFFEF5350)
                            null -> Color(0xFF9E9E9E).copy(alpha = 0.35f)
                        }
                    )
            )
        }
    }
}

package com.sterzhen.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sterzhen.app.ui.theme.Ink
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Выбор даты, с которой начинается отсчёт серии.
 * Будущие даты недоступны — серия не может начаться завтра.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartDateDialog(
    title: String,
    initial: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val selectable = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= System.currentTimeMillis()

            override fun isSelectableYear(year: Int): Boolean =
                year <= LocalDate.now().year
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (initial ?: LocalDate.now())
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli(),
        selectableDates = selectable
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(
            containerColor = Ink.Card,
            titleContentColor = Ink.TextDim,
            headlineContentColor = Ink.Text,
            weekdayContentColor = Ink.TextFaint,
            subheadContentColor = Ink.TextDim,
            navigationContentColor = Ink.TextDim,
            yearContentColor = Ink.TextDim,
            currentYearContentColor = Ink.Gold,
            selectedYearContentColor = Color(0xFF1A0A02),
            selectedYearContainerColor = Ink.Ember,
            dayContentColor = Ink.Text,
            selectedDayContentColor = Color(0xFF1A0A02),
            selectedDayContainerColor = Ink.Ember,
            todayContentColor = Ink.Gold,
            todayDateBorderColor = Ink.Gold,
            dividerColor = Color.White.copy(alpha = 0.08f)
        ),
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) {
                    onConfirm(
                        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    )
                } else {
                    onDismiss()
                }
            }) {
                Text("Готово", color = Ink.Ember, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Ink.TextDim)
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                color = Ink.Text,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp)
            )
            DatePicker(
                state = state,
                showModeToggle = false,
                title = null,
                headline = null
            )
            TextButton(
                onClick = { onConfirm(LocalDate.now()) },
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            ) {
                Text("Начать с сегодня", color = Ink.Mint, fontWeight = FontWeight.Bold)
            }
        }
    }
}

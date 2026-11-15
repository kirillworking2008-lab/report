package com.sterzhen.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sterzhen.app.ui.TaskTemplate
import com.sterzhen.app.ui.theme.Ink
import com.sterzhen.app.ui.theme.Radii

/**
 * Выбор шаблона: тап по шаблону разворачивает все его задачи в выбранный день.
 * Здесь же — создание нового шаблона из задач дня и удаление ненужных.
 */
@Composable
fun TemplatePickerDialog(
    templates: List<TaskTemplate>,
    dayLabel: String,
    dayTaskCount: Int,
    onPick: (TaskTemplate) -> Unit,
    onSaveDayAsTemplate: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(Radii.Hero))
                .background(Brush.verticalGradient(listOf(Ink.CardHi, Ink.Card)))
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.055f)
                        )
                    ),
                    RoundedCornerShape(Radii.Hero)
                )
                .padding(18.dp)
        ) {
            Text(
                text = "Шаблоны",
                style = MaterialTheme.typography.titleLarge,
                color = Ink.Ember
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Тап по шаблону добавит все его задачи в $dayLabel",
                style = MaterialTheme.typography.bodySmall,
                color = Ink.TextDim
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (templates.isEmpty()) {
                Text(
                    text = "Пока пусто. Собери первый шаблон — из текущих задач дня " +
                        "или с нуля.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink.TextFaint
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Column(
                    modifier = Modifier
                        .heightIn(max = 340.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    templates.forEach { template ->
                        TemplateRow(
                            template = template,
                            onPick = { onPick(template) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    GlowButton(
                        text = if (dayTaskCount > 0) "Из задач дня" else "Новый шаблон",
                        modifier = Modifier.fillMaxWidth()
                    ) { onSaveDayAsTemplate() }
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(Radii.Button))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.10f),
                            RoundedCornerShape(Radii.Button)
                        )
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", fontSize = 16.sp, color = Ink.TextDim)
                }
            }
        }
    }
}

@Composable
private fun TemplateRow(template: TaskTemplate, onPick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Card))
            .background(Color.White.copy(alpha = 0.045f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(Radii.Card))
            .clickable(onClick = onPick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                color = Ink.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = taskWord(template.items.size) + " · " +
                    template.items.take(3).joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = Ink.TextFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "+",
            fontSize = 18.sp,
            fontWeight = FontWeight(680),
            color = Ink.Ember
        )
    }
}

/** Ввод названия шаблона. */
@Composable
fun TemplateNameDialog(
    title: String,
    initial: String,
    hint: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initial) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(Radii.Hero))
                .background(Brush.verticalGradient(listOf(Ink.CardHi, Ink.Card)))
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.055f)
                        )
                    ),
                    RoundedCornerShape(Radii.Hero)
                )
                .padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink.Ember
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = Ink.TextDim
            )
            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radii.Field))
                    .background(Ink.Field)
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.10f),
                        RoundedCornerShape(Radii.Field)
                    )
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Например: Утро",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink.TextFaint
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink.Text),
                    cursorBrush = Brush.horizontalGradient(listOf(Ink.Ember, Ink.EmberDeep)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    GlowButton(
                        text = "Сохранить",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (value.isNotBlank()) onConfirm(value.trim())
                    }
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(Radii.Button))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.10f),
                            RoundedCornerShape(Radii.Button)
                        )
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", fontSize = 16.sp, color = Ink.TextDim)
                }
            }
        }
    }
}

/** Ввод пунктов нового шаблона (по одному в строке). */
@Composable
fun TemplateItemsDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var items by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(Radii.Hero))
                .background(Brush.verticalGradient(listOf(Ink.CardHi, Ink.Card)))
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.055f)
                        )
                    ),
                    RoundedCornerShape(Radii.Hero)
                )
                .padding(18.dp)
        ) {
            Text(
                text = "Новый шаблон",
                style = MaterialTheme.typography.titleLarge,
                color = Ink.Ember
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Название и задачи — каждая с новой строки",
                style = MaterialTheme.typography.bodySmall,
                color = Ink.TextDim
            )
            Spacer(modifier = Modifier.height(14.dp))

            FieldBox {
                if (name.isEmpty()) {
                    Text(
                        text = "Название, например: Утро",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink.TextFaint
                    )
                }
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink.Text),
                    cursorBrush = Brush.horizontalGradient(listOf(Ink.Ember, Ink.EmberDeep)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FieldBox(minHeight = 120.dp) {
                if (items.isEmpty()) {
                    Text(
                        text = "Зарядка\nДуш\nЧтение 20 минут",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink.TextFaint
                    )
                }
                BasicTextField(
                    value = items,
                    onValueChange = { items = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink.Text),
                    cursorBrush = Brush.horizontalGradient(listOf(Ink.Ember, Ink.EmberDeep)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    GlowButton(text = "Создать", modifier = Modifier.fillMaxWidth()) {
                        val lines = items.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                        if (name.isNotBlank() && lines.isNotEmpty()) {
                            onConfirm(name.trim(), lines)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(Radii.Button))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.10f),
                            RoundedCornerShape(Radii.Button)
                        )
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", fontSize = 16.sp, color = Ink.TextDim)
                }
            }
        }
    }
}

@Composable
private fun FieldBox(minHeight: androidx.compose.ui.unit.Dp = 0.dp, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(Radii.Field))
            .background(Ink.Field)
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(Radii.Field))
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        content()
    }
}

/** «1 задача / 3 задачи / 5 задач». */
private fun taskWord(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val word = when {
        mod10 == 1 && mod100 != 11 -> "задача"
        mod10 in 2..4 && mod100 !in 12..14 -> "задачи"
        else -> "задач"
    }
    return "$count $word"
}

package com.sterzhen.app.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.sterzhen.app.R
import com.sterzhen.app.data.Principles
import com.sterzhen.app.ui.components.AuroraBackground
import com.sterzhen.app.ui.components.OutlineButton
import com.sterzhen.app.ui.components.SectionHeader
import com.sterzhen.app.ui.theme.Ink
import com.sterzhen.app.ui.theme.SterzhenTheme

/** Экран выбора принципа при добавлении виджета на домашний экран. */
class WidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            SterzhenTheme {
                AuroraBackground {
                    Column(modifier = Modifier.fillMaxSize()) {
                        WidgetConfigScreen(
                            onPick = { principleId ->
                                StreakWidget.savePrinciple(
                                    applicationContext,
                                    appWidgetId,
                                    principleId
                                )
                                StreakWidget.refresh(applicationContext)
                                val result = Intent().putExtra(
                                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                                    appWidgetId
                                )
                                setResult(RESULT_OK, result)
                                finish()
                            },
                            onCancel = { finish() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetConfigScreen(onPick: (String) -> Unit, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader(
            title = stringResource(R.string.widget_config_title),
            caption = "Выбери принцип — виджет будет показывать серию именно по нему"
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Какой огонёк показывать?",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink.Ember
        )

        Spacer(modifier = Modifier.height(20.dp))

        Principles.ALL.forEach { principle ->
            val accent = Color(principle.colorHex)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(accent.copy(alpha = 0.12f), Color.White.copy(alpha = 0.03f))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(22.dp))
                    .clickable { onPick(principle.id) }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = principle.emoji, fontSize = 21.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = principle.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink.Text
                    )
                    Text(
                        text = principle.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.TextFaint
                    )
                }
                Text(
                    text = "→",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlineButton(
            text = "Отмена",
            modifier = Modifier.fillMaxWidth(),
            color = Ink.TextDim,
            onClick = onCancel
        )
        Spacer(modifier = Modifier.height(28.dp))
    }
}

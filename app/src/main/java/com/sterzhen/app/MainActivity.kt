package com.sterzhen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sterzhen.app.data.Repository
import com.sterzhen.app.data.db.AppDatabase
import com.sterzhen.app.ui.AppViewModel
import com.sterzhen.app.ui.components.AuroraBackground
import com.sterzhen.app.ui.screens.CoreScreen
import com.sterzhen.app.ui.screens.ReportsScreen
import com.sterzhen.app.ui.screens.TodayScreen
import com.sterzhen.app.ui.screens.WeekScreen
import com.sterzhen.app.ui.theme.Ink
import com.sterzhen.app.ui.theme.SterzhenTheme
import com.sterzhen.app.widget.StreakWidget

private data class NavItem(val label: String, val icon: ImageVector)

private val NAV_ITEMS = listOf(
    NavItem("Сегодня", Icons.Rounded.Home),
    NavItem("Неделя", Icons.Rounded.DateRange),
    NavItem("Стержень", Icons.Rounded.Whatshot),
    NavItem("Отчёты", Icons.Rounded.BarChart)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = Repository(AppDatabase.getInstance(this))
        StreakWidget.refresh(this)
        StreakWidget.scheduleNightlyUpdate(this)
        setContent {
            SterzhenTheme {
                val appContext = applicationContext
                val vm: AppViewModel = viewModel(
                    factory = AppViewModel.factory(repo) {
                        StreakWidget.refresh(appContext)
                    }
                )
                var tab by rememberSaveable { mutableStateOf(0) }

                AuroraBackground {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                            AnimatedContent(
                                targetState = tab,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(260)) +
                                        slideInVertically(
                                            animationSpec = tween(260),
                                            initialOffsetY = { it / 26 }
                                        )) togetherWith fadeOut(animationSpec = tween(140))
                                },
                                label = "tab"
                            ) { current ->
                                when (current) {
                                    0 -> TodayScreen(vm)
                                    1 -> WeekScreen(vm)
                                    2 -> CoreScreen(vm)
                                    else -> ReportsScreen(vm)
                                }
                            }
                        }

                        FloatingNav(
                            current = tab,
                            onSelect = { tab = it },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingNav(current: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.verticalGradient(listOf(Ink.CardHi, Ink.Card)))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(30.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NAV_ITEMS.forEachIndexed { index, item ->
            val selected = index == current
            val tint by animateColorAsState(
                targetValue = if (selected) Ink.Gold else Ink.TextFaint,
                animationSpec = tween(durationMillis = 220),
                label = "navTint"
            )
            val background = if (selected) {
                Modifier.background(
                    Brush.verticalGradient(
                        listOf(Ink.Ember.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
            } else {
                Modifier
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .then(background)
                    .clickable { onSelect(index) }
                    .padding(vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = tint,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

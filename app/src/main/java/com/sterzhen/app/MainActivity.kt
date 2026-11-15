package com.sterzhen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sterzhen.app.data.Repository
import com.sterzhen.app.data.db.AppDatabase
import com.sterzhen.app.ui.AppViewModel
import com.sterzhen.app.ui.screens.CoreScreen
import com.sterzhen.app.ui.screens.ReportsScreen
import com.sterzhen.app.ui.screens.TodayScreen
import com.sterzhen.app.ui.screens.WeekScreen
import com.sterzhen.app.ui.theme.SterzhenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = Repository(AppDatabase.getInstance(this))
        setContent {
            SterzhenTheme {
                val vm: AppViewModel = viewModel(factory = AppViewModel.factory(repo))
                var tab by rememberSaveable { mutableStateOf(0) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = tab == 0,
                                onClick = { tab = 0 },
                                icon = { Text("📅", fontSize = 22.sp) },
                                label = { Text("Сегодня") }
                            )
                            NavigationBarItem(
                                selected = tab == 1,
                                onClick = { tab = 1 },
                                icon = { Text("🗓", fontSize = 22.sp) },
                                label = { Text("Неделя") }
                            )
                            NavigationBarItem(
                                selected = tab == 2,
                                onClick = { tab = 2 },
                                icon = { Text("🔥", fontSize = 22.sp) },
                                label = { Text("Стержень") }
                            )
                            NavigationBarItem(
                                selected = tab == 3,
                                onClick = { tab = 3 },
                                icon = { Text("📊", fontSize = 22.sp) },
                                label = { Text("Отчёты") }
                            )
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (tab) {
                            0 -> TodayScreen(vm)
                            1 -> WeekScreen(vm)
                            2 -> CoreScreen(vm)
                            3 -> ReportsScreen(vm)
                        }
                    }
                }
            }
        }
    }
}

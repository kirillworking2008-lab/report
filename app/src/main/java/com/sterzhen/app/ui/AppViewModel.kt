package com.sterzhen.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sterzhen.app.data.DateUtils
import com.sterzhen.app.data.Principle
import com.sterzhen.app.data.Principles
import com.sterzhen.app.data.Repository
import com.sterzhen.app.data.computeBestStreak
import com.sterzhen.app.data.computeStreak
import com.sterzhen.app.data.db.PrincipleCheckEntity
import com.sterzhen.app.data.db.TaskEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PrincipleState(
    val principle: Principle,
    val currentStreak: Int,
    val bestStreak: Int,
    val keptToday: Boolean,
    /** Последние 14 дней, от старых к новым. null = не отмечено. */
    val lastDays: List<Boolean?>
)

class AppViewModel(private val repo: Repository) : ViewModel() {

    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today

    init {
        // Автообновление в полночь: стрик-огоньки пересчитываются
        // от дат, поэтому достаточно обновить «сегодня».
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                val now = LocalDate.now()
                if (now != _today.value) {
                    _today.value = now
                }
            }
        }
    }

    val tasks: StateFlow<List<TaskEntity>> = repo.allTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val checks: StateFlow<List<PrincipleCheckEntity>> = repo.allChecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val principleStates: StateFlow<List<PrincipleState>> =
        combine(checks, today) { list, t ->
            val byPrinciple = list.groupBy { it.principleId }
            Principles.ALL.map { p ->
                val entries = byPrinciple[p.id].orEmpty()
                val keptDates = entries.filter { it.kept }.map { it.date }.toSet()
                val keptMap = entries.associate { it.date to it.kept }
                val lastDays = (13 downTo 0).map { offset ->
                    keptMap[DateUtils.format(t.minusDays(offset.toLong()))]
                }
                PrincipleState(
                    principle = p,
                    currentStreak = computeStreak(keptDates, t),
                    bestStreak = computeBestStreak(keptDates),
                    keptToday = keptMap[DateUtils.format(t)] == true,
                    lastDays = lastDays
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addTask(title: String, date: LocalDate) {
        val text = title.trim()
        if (text.isEmpty()) return
        viewModelScope.launch { repo.addTask(text, DateUtils.format(date)) }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch { repo.setTaskDone(task.id, !task.done) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repo.deleteTask(task) }
    }

    fun toggleKept(principleId: String) {
        val date = DateUtils.format(_today.value)
        val currentlyKept = checks.value.any {
            it.principleId == principleId && it.date == date && it.kept
        }
        viewModelScope.launch { repo.setKept(principleId, date, !currentlyKept) }
    }

    companion object {
        fun factory(repo: Repository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppViewModel(repo) as T
                }
            }
    }
}

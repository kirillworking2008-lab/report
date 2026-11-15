package com.sterzhen.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sterzhen.app.data.DateUtils
import com.sterzhen.app.data.Principle
import com.sterzhen.app.data.Principles
import com.sterzhen.app.data.Repository
import com.sterzhen.app.data.autoBestStreak
import com.sterzhen.app.data.autoStreak
import com.sterzhen.app.data.dayState
import com.sterzhen.app.data.db.PrincipleMetaEntity
import com.sterzhen.app.data.db.PrincipleSlipEntity
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
    /** Текущий стрик в днях, обновляется сам каждый день. */
    val currentStreak: Int,
    val bestStreak: Int,
    /** Срыв отмечен сегодня. */
    val slippedToday: Boolean,
    /** Принцип уже отслеживается (есть точка старта). */
    val tracked: Boolean,
    /** Последние 14 дней, от старых к новым. null = ещё не отслеживался. */
    val lastDays: List<Boolean?>,
    /** Дата, с которой ведётся отсчёт (можно менять вручную). */
    val startDate: LocalDate? = null
)

class AppViewModel(
    private val repo: Repository,
    private val onDataChanged: () -> Unit = {}
) : ViewModel() {

    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today

    init {
        viewModelScope.launch { repo.ensureSeeded() }

        // Автообновление огоньков: стрик считается от дат, поэтому
        // достаточно заметить смену суток — в полночь стрик вырастет сам.
        viewModelScope.launch {
            while (true) {
                delay(20_000)
                val now = LocalDate.now()
                if (now != _today.value) _today.value = now
            }
        }
    }

    val tasks: StateFlow<List<TaskEntity>> = repo.allTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val slips: StateFlow<List<PrincipleSlipEntity>> = repo.slips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val meta: StateFlow<List<PrincipleMetaEntity>> = repo.meta()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val principleStates: StateFlow<List<PrincipleState>> =
        combine(repo.slips(), repo.meta(), today) { slipList, metaList, t ->
            val startById = metaList.associate { it.principleId to DateUtils.parse(it.startDate) }
            val slipsById = slipList.groupBy { it.principleId }

            Principles.ALL.map { p ->
                val start = startById[p.id]
                val slips = slipsById[p.id].orEmpty().map { DateUtils.parse(it.date) }.toSet()
                if (start == null) {
                    PrincipleState(p, 0, 0, false, false, List(14) { null })
                } else {
                    PrincipleState(
                        principle = p,
                        currentStreak = autoStreak(start, slips, t),
                        bestStreak = autoBestStreak(start, slips, t),
                        slippedToday = t in slips,
                        tracked = true,
                        lastDays = (13 downTo 0).map { offset ->
                            dayState(t.minusDays(offset.toLong()), start, slips)
                        },
                        startDate = start
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Сколько принципов сегодня без срыва (из отслеживаемых). */
    val heldTodayCount: StateFlow<Int> =
        combine(principleStates, today) { states, _ ->
            states.count { it.tracked && !it.slippedToday }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun addTask(title: String, date: LocalDate) {
        val text = title.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            repo.addTask(text, DateUtils.format(date))
            onDataChanged()
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repo.setTaskDone(task.id, !task.done)
            onDataChanged()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repo.deleteTask(task)
            onDataChanged()
        }
    }

    /** Поменять дату начала отсчёта серии. */
    fun setStartDate(principleId: String, date: LocalDate) {
        viewModelScope.launch {
            repo.setStartDate(principleId, DateUtils.format(date))
            onDataChanged()
        }
    }

    /** Отметить/снять срыв на сегодня. Всё остальное считается само. */
    fun toggleSlip(principleId: String, slippedToday: Boolean, date: LocalDate = _today.value) {
        viewModelScope.launch {
            repo.toggleSlip(principleId, DateUtils.format(date), slippedToday)
            onDataChanged()
        }
    }

    companion object {
        fun factory(
            repo: Repository,
            onDataChanged: () -> Unit = {}
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppViewModel(repo, onDataChanged) as T
                }
            }
    }
}

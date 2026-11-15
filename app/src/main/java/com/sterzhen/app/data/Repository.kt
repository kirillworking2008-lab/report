package com.sterzhen.app.data

import com.sterzhen.app.data.db.AppDatabase
import com.sterzhen.app.data.db.PrincipleMetaEntity
import com.sterzhen.app.data.db.PrincipleSlipEntity
import com.sterzhen.app.data.db.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class Repository(private val db: AppDatabase) {

    fun tasksForDate(date: String): Flow<List<TaskEntity>> =
        db.taskDao().tasksForDate(date)

    fun allTasks(): Flow<List<TaskEntity>> = db.taskDao().allTasks()

    fun slips(): Flow<List<PrincipleSlipEntity>> = db.principleDao().slips()

    fun meta(): Flow<List<PrincipleMetaEntity>> = db.principleDao().meta()

    /** Первый запуск: каждому принципу ставим точку отсчёта — сегодня. */
    suspend fun ensureSeeded() {
        val existing = db.principleDao().metaOnce().map { it.principleId }.toSet()
        val today = DateUtils.format(LocalDate.now())
        Principles.ALL
            .filter { it.id !in existing }
            .forEach { db.principleDao().insertMeta(PrincipleMetaEntity(it.id, today)) }
    }

    suspend fun addTask(title: String, date: String) {
        db.taskDao().insert(TaskEntity(title = title, date = date))
    }

    suspend fun setTaskDone(id: Long, done: Boolean) = db.taskDao().setDone(id, done)

    suspend fun deleteTask(task: TaskEntity) = db.taskDao().delete(task)

    /**
     * Срыв — событие, которое ломает серию. Всё остальное считается
     * автоматически, поэтому больше ничего отмечать не нужно.
     */
    suspend fun toggleSlip(principleId: String, date: String, exists: Boolean) {
        if (exists) {
            db.principleDao().deleteSlip(principleId, date)
        } else {
            db.principleDao().upsertSlip(PrincipleSlipEntity(principleId, date))
        }
    }
}

object DateUtils {
    private val fmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun format(date: LocalDate): String = date.format(fmt)

    fun parse(value: String): LocalDate = LocalDate.parse(value, fmt)
}

/**
 * Текущий стрик в днях. Считается автоматически: от дня после последнего
 * срыва (или от старта) до сегодня включительно.
 */
fun autoStreak(start: LocalDate, slips: Set<LocalDate>, today: LocalDate): Int {
    if (today < start) return 0
    val lastSlip = slips.filter { it <= today }.maxOrNull() ?: start.minusDays(1)
    val from = maxOf(lastSlip.plusDays(1), start)
    val days = ChronoUnit.DAYS.between(from, today).toInt()
    return if (days < 0) 0 else days + 1
}

/** Лучшая серия за всё время. */
fun autoBestStreak(start: LocalDate, slips: Set<LocalDate>, today: LocalDate): Int {
    if (today < start) return 0
    val relevant = slips.filter { it in start..today }.sorted()
    var best = 0
    var cursor = start
    relevant.forEach { slip ->
        val len = ChronoUnit.DAYS.between(cursor, slip).toInt()
        if (len > best) best = len
        cursor = slip.plusDays(1)
    }
    val tail = ChronoUnit.DAYS.between(cursor, today).toInt() + 1
    if (tail > best) best = tail
    return best
}

/** Состояние дня: true — выдержал, false — срыв, null — ещё не отслеживался. */
fun dayState(day: LocalDate, start: LocalDate, slips: Set<LocalDate>): Boolean? = when {
    day < start -> null
    day in slips -> false
    else -> true
}

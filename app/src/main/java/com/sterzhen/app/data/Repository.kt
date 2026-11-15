package com.sterzhen.app.data

import com.sterzhen.app.data.db.AppDatabase
import com.sterzhen.app.data.db.PrincipleCheckEntity
import com.sterzhen.app.data.db.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Repository(private val db: AppDatabase) {

    fun tasksForDate(date: String): Flow<List<TaskEntity>> =
        db.taskDao().tasksForDate(date)

    fun allTasks(): Flow<List<TaskEntity>> = db.taskDao().allTasks()

    fun allChecks(): Flow<List<PrincipleCheckEntity>> = db.principleCheckDao().allChecks()

    suspend fun addTask(title: String, date: String) {
        db.taskDao().insert(TaskEntity(title = title, date = date))
    }

    suspend fun setTaskDone(id: Long, done: Boolean) = db.taskDao().setDone(id, done)

    suspend fun deleteTask(task: TaskEntity) = db.taskDao().delete(task)

    suspend fun setKept(principleId: String, date: String, kept: Boolean) {
        if (kept) {
            db.principleCheckDao().upsert(PrincipleCheckEntity(principleId, date, true))
        } else {
            db.principleCheckDao().deleteCheck(principleId, date)
        }
    }
}

object DateUtils {
    private val fmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun format(date: LocalDate): String = date.format(fmt)

    fun parse(value: String): LocalDate = LocalDate.parse(value, fmt)
}

/**
 * Текущий стрик: серия подряд идущих дней, когда принцип держался.
 * Если сегодня ещё не отмечено — стрик считается до вчера
 * (день не сгорел, пока не закончился).
 */
fun computeStreak(keptDates: Set<String>, today: LocalDate): Int {
    var streak = 0
    var day = today
    if (DateUtils.format(day) !in keptDates) {
        day = day.minusDays(1)
    }
    while (DateUtils.format(day) in keptDates) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}

/** Лучший стрик за всё время. */
fun computeBestStreak(keptDates: Set<String>): Int {
    if (keptDates.isEmpty()) return 0
    val sorted = keptDates.map { DateUtils.parse(it) }.sorted()
    var best = 1
    var run = 1
    for (i in 1 until sorted.size) {
        if (sorted[i] == sorted[i - 1].plusDays(1)) {
            run++
            if (run > best) best = run
        } else {
            run = 1
        }
    }
    return best
}

package com.sterzhen.app.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** Дата в формате yyyy-MM-dd */
    val date: String,
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "principle_checks", primaryKeys = ["principleId", "date"])
data class PrincipleCheckEntity(
    val principleId: String,
    /** Дата в формате yyyy-MM-dd */
    val date: String,
    val kept: Boolean
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY createdAt ASC")
    fun tasksForDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY date ASC, createdAt ASC")
    fun allTasks(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity)

    @Query("UPDATE tasks SET done = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Delete
    suspend fun delete(task: TaskEntity)
}

@Dao
interface PrincipleCheckDao {
    @Query("SELECT * FROM principle_checks")
    fun allChecks(): Flow<List<PrincipleCheckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(check: PrincipleCheckEntity)

    @Query("DELETE FROM principle_checks WHERE principleId = :principleId AND date = :date")
    suspend fun deleteCheck(principleId: String, date: String)
}

@Database(
    entities = [TaskEntity::class, PrincipleCheckEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun principleCheckDao(): PrincipleCheckDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sterzhen.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

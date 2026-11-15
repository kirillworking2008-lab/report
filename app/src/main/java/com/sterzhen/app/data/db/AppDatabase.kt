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

/**
 * Срыв — единственное, что нужно отмечать руками.
 * Все остальные дни считаются «выдержанными» автоматически.
 */
@Entity(tableName = "principle_slips", primaryKeys = ["principleId", "date"])
data class PrincipleSlipEntity(
    val principleId: String,
    /** Дата в формате yyyy-MM-dd */
    val date: String
)

/** С какого дня ведём отсчёт стрика по принципу. */
@Entity(tableName = "principle_meta")
data class PrincipleMetaEntity(
    @PrimaryKey val principleId: String,
    val startDate: String
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
interface PrincipleDao {
    @Query("SELECT * FROM principle_slips")
    fun slips(): Flow<List<PrincipleSlipEntity>>

    @Query("SELECT * FROM principle_meta")
    fun meta(): Flow<List<PrincipleMetaEntity>>

    @Query("SELECT * FROM principle_meta")
    suspend fun metaOnce(): List<PrincipleMetaEntity>

    @Query("SELECT * FROM principle_slips")
    suspend fun slipsOnce(): List<PrincipleSlipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlip(slip: PrincipleSlipEntity)

    @Query("DELETE FROM principle_slips WHERE principleId = :principleId AND date = :date")
    suspend fun deleteSlip(principleId: String, date: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeta(meta: PrincipleMetaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeta(meta: PrincipleMetaEntity)
}

@Database(
    entities = [
        TaskEntity::class,
        PrincipleSlipEntity::class,
        PrincipleMetaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun principleDao(): PrincipleDao

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

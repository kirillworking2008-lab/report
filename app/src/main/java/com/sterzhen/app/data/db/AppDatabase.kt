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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

/**
 * Шаблон набора задач: «Утро», «Спорт», «Перед сном» — что угодно,
 * что повторяется изо дня в день.
 */
@Entity(tableName = "task_templates")
data class TaskTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_template_items")
data class TaskTemplateItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val title: String,
    val position: Int = 0
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

    /** «Прокрастинация» переименована в «курение» — переносим историю срывов. */
    @Query("UPDATE principle_slips SET principleId = 'smoking' WHERE principleId = 'procrastination'")
    suspend fun migrateProcrastinationSlips()

    @Query("UPDATE principle_meta SET principleId = 'smoking' WHERE principleId = 'procrastination'")
    suspend fun migrateProcrastinationMeta()
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM task_templates ORDER BY createdAt DESC")
    fun templates(): Flow<List<TaskTemplateEntity>>

    @Query("SELECT * FROM task_template_items ORDER BY position ASC")
    fun allItems(): Flow<List<TaskTemplateItemEntity>>

    @Query("SELECT * FROM task_template_items WHERE templateId = :templateId ORDER BY position ASC")
    suspend fun itemsFor(templateId: Long): List<TaskTemplateItemEntity>

    @Insert
    suspend fun insertTemplate(template: TaskTemplateEntity): Long

    @Insert
    suspend fun insertItems(items: List<TaskTemplateItemEntity>)

    @Query("DELETE FROM task_template_items WHERE templateId = :templateId")
    suspend fun clearItems(templateId: Long)

    @Query("DELETE FROM task_templates WHERE id = :templateId")
    suspend fun deleteTemplate(templateId: Long)
}

@Database(
    entities = [
        TaskEntity::class,
        PrincipleSlipEntity::class,
        PrincipleMetaEntity::class,
        TaskTemplateEntity::class,
        TaskTemplateItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun principleDao(): PrincipleDao
    abstract fun templateDao(): TemplateDao

    companion object {
        /** 1 → 2: таблицы шаблонов задач. Данные пользователя не трогаем. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `task_templates` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `task_template_items` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`templateId` INTEGER NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`position` INTEGER NOT NULL)"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sterzhen.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

package com.sterzhen.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.sterzhen.app.MainActivity
import com.sterzhen.app.R
import com.sterzhen.app.data.DateUtils
import com.sterzhen.app.data.autoStreak
import com.sterzhen.app.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Виджет 1×1: огонёк и число дней лучшей серии.
 * Обновляется сам: в полночь (AlarmManager), при изменении данных в приложении
 * и после перезагрузки телефона.
 */
object StreakWidget {

    const val ACTION_REFRESH = "com.sterzhen.app.WIDGET_REFRESH"
    private const val ALARM_REQUEST_CODE = 4242

    /** Перерисовать все экземпляры виджета. */
    fun refresh(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val component = ComponentName(appContext, StreakWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            val streak = bestStreak(appContext)
            ids.forEach { id ->
                manager.updateAppWidget(id, buildViews(appContext, streak))
            }
        }
    }

    /** Самая длинная текущая серия среди всех принципов. */
    suspend fun bestStreak(context: Context): Int {
        val dao = AppDatabase.getInstance(context).principleDao()
        val today = LocalDate.now()
        val slipsByPrinciple = dao.slipsOnce().groupBy { it.principleId }
        return dao.metaOnce().maxOfOrNull { meta ->
            val start = DateUtils.parse(meta.startDate)
            val slips = slipsByPrinciple[meta.principleId]
                .orEmpty()
                .map { DateUtils.parse(it.date) }
                .toSet()
            autoStreak(start, slips, today)
        } ?: 0
    }

    private fun buildViews(context: Context, streak: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_streak)
        views.setTextViewText(R.id.widget_number, streak.toString())
        views.setTextColor(R.id.widget_number, numberColor(streak))
        views.setTextViewText(R.id.widget_flame, if (streak > 0) "🔥" else "🧊")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pending)
        return views
    }

    private fun numberColor(streak: Int): Int = when {
        streak >= 100 -> Color.parseColor("#FF2D55")
        streak >= 30 -> Color.parseColor("#FF5722")
        streak >= 7 -> Color.parseColor("#FF7A2F")
        streak > 0 -> Color.parseColor("#FFC46B")
        else -> Color.parseColor("#6EA8FF")
    }

    /** Будильник на ближайшую полночь: стрик сам вырастет на 1. */
    fun scheduleNightlyUpdate(context: Context) {
        val appContext = context.applicationContext
        val manager = appContext.getSystemService(AlarmManager::class.java) ?: return
        val nextMidnight = LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() + 5_000L

        try {
            manager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextMidnight,
                alarmIntent(appContext)
            )
        } catch (e: SecurityException) {
            manager.set(AlarmManager.RTC_WAKEUP, nextMidnight, alarmIntent(appContext))
        }
    }

    fun cancelNightlyUpdate(context: Context) {
        val manager = context.applicationContext
            .getSystemService(AlarmManager::class.java) ?: return
        manager.cancel(alarmIntent(context.applicationContext))
    }

    private fun alarmIntent(context: Context): PendingIntent {
        val intent = Intent(context, StreakWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

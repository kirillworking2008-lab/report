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
import com.sterzhen.app.data.Principle
import com.sterzhen.app.data.Principles
import com.sterzhen.app.data.autoStreak
import com.sterzhen.app.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Виджет «Огонёк» 2×1 — по одному на принцип.
 * При добавлении на экран открывается выбор принципа, подпись и огонёк
 * показывают именно его серию.
 */
object StreakWidget {

    const val ACTION_REFRESH = "com.sterzhen.app.WIDGET_REFRESH"
    private const val ALARM_REQUEST_CODE = 4242
    private const val PREFS = "sterzhen_widgets"

    // ---- привязка виджета к принципу ----

    fun savePrinciple(context: Context, appWidgetId: Int, principleId: String) {
        prefs(context).edit().putString("p_$appWidgetId", principleId).apply()
    }

    fun principleId(context: Context, appWidgetId: Int): String? =
        prefs(context).getString("p_$appWidgetId", null)

    fun clearPrinciple(context: Context, appWidgetId: Int) {
        prefs(context).edit().remove("p_$appWidgetId").apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ---- обновление ----

    /** Перерисовать все экземпляры виджета (вызывается из приложения). */
    fun refresh(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch { render(appContext) }
    }

    /** Перерисовать все экземпляры и дождаться результата. */
    suspend fun render(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val ids = manager.getAppWidgetIds(
            ComponentName(appContext, StreakWidgetProvider::class.java)
        )
        if (ids.isEmpty()) return

        val dao = AppDatabase.getInstance(appContext).principleDao()
        val today = LocalDate.now()
        val metaById = dao.metaOnce().associateBy { it.principleId }
        val slipsByPrinciple = dao.slipsOnce().groupBy { it.principleId }

        ids.forEach { id ->
            // Если принцип не выбран, раздаём их по кругу: первый виджет — алкоголь,
            // второй — энергетики и т.д., вместо «всегда алкоголь».
            val fallbackIndex = ids.indexOf(id).mod(Principles.ALL.size)
            val principle = Principles.byId(principleId(appContext, id) ?: "")
                ?: Principles.ALL[fallbackIndex]
            val start = metaById[principle.id]?.let { DateUtils.parse(it.startDate) }
            val slips = slipsByPrinciple[principle.id]
                .orEmpty()
                .map { DateUtils.parse(it.date) }
                .toSet()
            val streak = if (start != null) autoStreak(start, slips, today) else 0
            manager.updateAppWidget(id, buildViews(appContext, id, principle, streak))
        }
    }

    private fun buildViews(
        context: Context,
        appWidgetId: Int,
        principle: Principle,
        streak: Int
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_streak)
        views.setTextViewText(R.id.widget_emoji, if (streak > 0) principle.emoji else "🧊")
        views.setTextViewText(R.id.widget_name, principle.title)
        views.setTextViewText(R.id.widget_number, streak.toString())
        views.setTextColor(R.id.widget_number, numberColor(streak))

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

        // шестерёнка: открыть выбор принципа именно для этого виджета
        val settings = Intent(context, WidgetConfigActivity::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val settingsPending = PendingIntent.getActivity(
            context,
            1000 + appWidgetId,
            settings,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_settings, settingsPending)
        return views
    }

    /** Цвета огонька из дизайн-кита Running v1.0. */
    private fun numberColor(streak: Int): Int = when {
        streak >= 100 -> Color.parseColor("#B9A4FF")  // violet
        streak >= 30 -> Color.parseColor("#FFB376")   // amber
        streak >= 7 -> Color.parseColor("#73E3CF")    // mint
        streak > 0 -> Color.parseColor("#F4F7F8")     // ink
        else -> Color.parseColor("#56636B")           // faint
    }

    // ---- будильник на полночь ----

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

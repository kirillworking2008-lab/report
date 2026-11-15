package com.sterzhen.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreakWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        renderAsync(context, goAsync())
        StreakWidget.scheduleNightlyUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == StreakWidget.ACTION_REFRESH) {
            renderAsync(context, goAsync())
            StreakWidget.scheduleNightlyUpdate(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        StreakWidget.refresh(context)
        StreakWidget.scheduleNightlyUpdate(context)
    }

    private fun renderAsync(context: Context, pending: PendingResult?) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                StreakWidget.render(appContext)
            } finally {
                pending?.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { StreakWidget.clearPrinciple(context, it) }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        StreakWidget.cancelNightlyUpdate(context)
    }
}

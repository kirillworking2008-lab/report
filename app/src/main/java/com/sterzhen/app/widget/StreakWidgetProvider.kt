package com.sterzhen.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class StreakWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        StreakWidget.refresh(context)
        StreakWidget.scheduleNightlyUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == StreakWidget.ACTION_REFRESH) {
            StreakWidget.refresh(context)
            StreakWidget.scheduleNightlyUpdate(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        StreakWidget.refresh(context)
        StreakWidget.scheduleNightlyUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        StreakWidget.cancelNightlyUpdate(context)
    }
}

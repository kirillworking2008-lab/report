package com.sterzhen.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** После перезагрузки телефона будильники теряются — ставим заново. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            StreakWidget.refresh(context)
            StreakWidget.scheduleNightlyUpdate(context)
        }
    }
}

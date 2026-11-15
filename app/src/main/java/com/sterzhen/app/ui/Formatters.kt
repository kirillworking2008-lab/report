package com.sterzhen.app.ui

import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

val RU_LOCALE: Locale = Locale("ru")

/** Короткое название дня недели на русском: пн, вт, ср... */
fun dayShortName(day: DayOfWeek): String = day.getDisplayName(TextStyle.SHORT, RU_LOCALE)

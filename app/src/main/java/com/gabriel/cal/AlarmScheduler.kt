package com.gabriel.cal

import android.content.Context
import android.util.Log
import java.util.*

object AlarmScheduler {
    private const val PREFS_NAME = "alarm_prefs"
    private const val KEY_DATES = "selected_dates"  // Por simplicidad, una cadena separada por comas

    // Recupera el conjunto de fechas guardadas
    fun loadDates(context: Context): Set<Long> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_DATES, "") ?: ""
        return if (stored.isEmpty()) {
            emptySet()
        } else {
            stored.split(",").mapNotNull { it.toLongOrNull() }.toSet()
        }
    }

    // Reprograma todas las alarmas basándose en las fechas guardadas
    fun reScheduleAlarms(context: Context) {
        val dates = loadDates(context)
        Log.d("AlarmScheduler", "Reprogramando alarmas para fechas: $dates")
        dates.forEach { dateInMillis ->
            // Programa la alarma para cada fecha (se ajusta la hora a las 5 PM)
            AlarmHelper.scheduleAlarmForDate(context, dateInMillis)
        }
    }
}

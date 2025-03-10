package com.gabriel.cal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Reinicio detectado: reprogramando alarmas.")

            // Aquí debes reprogramar las alarmas utilizando los datos persistidos.
            // Por ejemplo, podrías leer las fechas guardadas en Firestore o en SharedPreferences.
            // Para efectos de ejemplo, supongamos que tienes una función que reprograma alarmas:
            AlarmScheduler.reScheduleAlarms(context)
        }
    }
}

package com.gabriel.cal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Por ejemplo, mostramos un Toast cuando se dispara la alarma.
        val alarmDate = intent.getLongExtra("alarm_date", 0L)
        Toast.makeText(context, "Alarma disparada para: $alarmDate", Toast.LENGTH_LONG).show()

        // Aquí puedes implementar la lógica que desees, como mostrar una notificación.
    }
}

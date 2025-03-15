package com.gabriel.cal

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmDate = intent.getLongExtra("alarm_date", 0L)

        // Crear un PendingIntent para abrir la app cuando el usuario toca la notificación.
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val notificationBuilder = NotificationCompat.Builder(context, "macro_notifications")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Asegúrate de disponer de un recurso de ícono adecuado
            .setContentTitle("Macro Programada")
            .setContentText("Tienes una macro asignada para hoy a la hora programada.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Mostrar la notificación
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(context)) {
                notify(alarmDate.hashCode(), notificationBuilder.build())
            }
        } else {
            // Manejar el caso en que no se tiene permiso, por ejemplo, logueando el incidente
            Log.w("AlarmReceiver", "Permiso POST_NOTIFICATIONS no concedido")
        }
    }
}

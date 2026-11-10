package com.nexusdigital.admin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

/**
 * Crea el canal de notificaciones (con sonido y vibración) en cuanto arranca la app,
 * ANTES de que llegue cualquier notificación. Esto es importante: si Android recibe
 * una notificación con la app cerrada y el canal todavía no existe, la muestra sin
 * sonido (canal por defecto). Creándolo aquí nos aseguramos de que ya exista desde
 * la primera notificación, incluso con la app nunca abierta todavía.
 */
class NexusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val administrador = getSystemService(NotificationManager::class.java)

            val sonidoNotificacion = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val atributosAudio = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val canal = NotificationChannel(
                "chat_nexus",
                "Chat en vivo — NEXUS DIGITAL",
                NotificationManager.IMPORTANCE_HIGH
            )
            canal.description = "Avisa de chats nuevos, formularios de contacto y mensajes."
            canal.enableVibration(true)
            canal.vibrationPattern = longArrayOf(0, 300, 150, 300)
            canal.setSound(sonidoNotificacion, atributosAudio)
            canal.setShowBadge(true)

            administrador.createNotificationChannel(canal)
        }
    }
}

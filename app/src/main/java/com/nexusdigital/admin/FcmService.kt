package com.nexusdigital.admin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

/**
 * Recibe las notificaciones nativas que manda el servidor (vía Firebase Cloud Messaging)
 * cuando llega un chat nuevo, un formulario de contacto, o un mensaje de chat.
 * Funciona aunque la app esté cerrada o en segundo plano, con sonido y vibración del sistema.
 */
class FcmService : FirebaseMessagingService() {

    override fun onMessageReceived(mensaje: RemoteMessage) {
        val titulo = mensaje.notification?.title ?: mensaje.data["titulo"] ?: "NEXUS DIGITAL"
        val cuerpo = mensaje.notification?.body ?: mensaje.data["cuerpo"] ?: ""
        val url = mensaje.data["url"]

        mostrarNotificacion(titulo, cuerpo, url)
    }

    override fun onNewToken(token: String) {
        // El token nuevo se registra la próxima vez que se abra una pantalla del admin
        // dentro de la app (MainActivity vuelve a pedirlo y lo manda por JS).
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String, url: String?) {
        val canalId = "chat_nexus"
        val administrador = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Chat en vivo — NEXUS DIGITAL",
                NotificationManager.IMPORTANCE_HIGH
            )
            canal.enableVibration(true)
            canal.vibrationPattern = longArrayOf(0, 300, 150, 300)
            canal.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            administrador.createNotificationChannel(canal)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (url != null) putExtra(MainActivity.EXTRA_URL_ABRIR, url)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, Random.nextInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .build()

        administrador.notify(Random.nextInt(), notificacion)
    }
}

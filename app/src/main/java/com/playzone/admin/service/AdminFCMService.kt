package com.playzone.admin.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.playzone.admin.MainActivity
import com.playzone.admin.R

class AdminFCMService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_BOOKING = "channel_booking"
        const val CHANNEL_SNACK   = "channel_snack"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_BOOKING, "Booking Baru", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Notifikasi saat pelanggan membuat booking baru"
                        enableVibration(true)
                    }
                )
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_SNACK, "Order Snack Baru", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Notifikasi saat pelanggan memesan snack"
                        enableVibration(true)
                    }
                )
            }
        }

        fun showBookingNotification(context: Context, title: String, message: String) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("tab", "queue")
            }
            val pi = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notif = NotificationCompat.Builder(context, CHANNEL_BOOKING)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build()
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(System.currentTimeMillis().toInt(), notif)
        }

        fun showSnackNotification(context: Context, title: String, message: String) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("tab", "snack")
            }
            val pi = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notif = NotificationCompat.Builder(context, CHANNEL_SNACK)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build()
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(System.currentTimeMillis().toInt(), notif)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Playzone Admin"
        val body  = message.notification?.body  ?: message.data["body"]  ?: ""
        val type  = message.data["type"] ?: "booking"

        if (type == "snack") {
            showSnackNotification(this, title, body)
        } else {
            showBookingNotification(this, title, body)
        }
    }

    override fun onNewToken(token: String) {
        // Token baru — bisa disimpan ke Firestore jika perlu
    }
}

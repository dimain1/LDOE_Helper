package com.example.kotlinclient.presentation.utility

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.kotlinclient.state_management.entity.Event

object NotificationHelper {

    private const val CHANNEL_ID = "Event_channel"

    fun showEventFinished(context: Context, eventId: String, eventName: String) {

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Event notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Событие завершено")
            .setContentText("Событие: ${eventName} Завершено")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(eventId.hashCode(), notification)
    }



}
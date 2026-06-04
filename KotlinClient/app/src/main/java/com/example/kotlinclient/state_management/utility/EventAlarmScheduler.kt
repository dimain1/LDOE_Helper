package com.example.kotlinclient.state_management.utility

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.kotlinclient.state_management.entity.Event
import java.time.ZoneId

class EventAlarmScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleFinish(event: Event) {
        val intent = Intent(context, EventFinishReceiver::class.java).apply {
            putExtra(EventFinishReceiver.EXTRA_EVENT_ID, event.id.toString())
            putExtra(EventFinishReceiver.EXTRA_EVENT_NAME, event.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.toString().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            event.endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingIntent
        )
    }

    fun cancel(eventId: Long) {
        val intent = Intent(context, EventFinishReceiver::class.java).apply {
            putExtra(EventFinishReceiver.EXTRA_EVENT_ID, eventId.toString())
            putExtra(EventFinishReceiver.EXTRA_EVENT_NAME, "")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toString().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

}
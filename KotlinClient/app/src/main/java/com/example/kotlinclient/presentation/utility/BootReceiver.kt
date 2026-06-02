package com.example.kotlinclient.presentation.utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Получает ACTION_BOOT_COMPLETED и переплановывает все активные аларм-таймеры.
 *
 * После перезагрузки Android сбрасывает все AlarmManager-задачи.
 * Этот ресивер восстанавливает их для всех событий из локальной базы,
 * время окончания которых ещё не наступило.
 */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        // Обрабатываем стандартную перезагрузку и быстрое включение (Xiaomi/MIUI)
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON") return

        val database: AppDatabase by inject()
        val sharedPrefs: SharedPreferencesRepository by inject()

        val userId = sharedPrefs.getLongByKey("user_id")
        if (userId == -1L) return          // пользователь не залогинен — нечего восстанавливать

        val alarmScheduler = EventAlarmScheduler(context)
        val nowMillis = Instant.now().toEpochMilli()

        // Синхронный запрос допустим здесь, т.к. allowMainThreadQueries() выставлен в AppDatabase
        val activeEvents = database.EventDao().getActiveEventsSync(userId, nowMillis)

        activeEvents.forEach { entity ->
            val event = Event(
                id = entity.id,
                user = null,
                template = null,
                name = entity.name,
                description = entity.description,
                image = entity.imageUrl,
                startTime = LocalDateTime.ofInstant(entity.startTime, ZoneId.systemDefault()),
                endTime = LocalDateTime.ofInstant(entity.endTime, ZoneId.systemDefault())
            )
            alarmScheduler.scheduleFinish(event)
        }
    }
}

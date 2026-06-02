package com.example.kotlinclient.presentation.utility

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EventFinishReceiver() :
    BroadcastReceiver(), KoinComponent {

    @SuppressLint("SuspiciousIndentation")
    override fun onReceive(context: Context, intent: Intent) {

        val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: return
        val eventName = intent.getStringExtra(EXTRA_EVENT_NAME) ?: return

        val sharedPreferencesRepository: SharedPreferencesRepository by inject()
        val shouldNotify = sharedPreferencesRepository.getBooleanByKey("notification")

            if (shouldNotify) {
                NotificationHelper.showEventFinished(context, eventId, eventName)
            }
    }

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_EVENT_NAME = "extra_event_name"
    }

}
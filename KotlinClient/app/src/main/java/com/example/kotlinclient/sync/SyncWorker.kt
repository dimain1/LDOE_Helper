package com.example.kotlinclient.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kotlinclient.api_client.TokenStorage
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Фоновый воркер (WorkManager) — выполняется при наличии сети.
 *
 * Логика разделена на две группы:
 *  • Публичные операции (без авторизации): GameContent, ContentType — выполняются всегда.
 *  • Авторизованные операции (Events, Templates) — только если есть токены.
 *
 * Порядок push-операций важен для FK:
 *   1. Шаблоны (EventTemplate) — раньше событий (события ссылаются на их serverId).
 *   2. Затем события (Event).
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val eventRepository:         EventRepository         by inject()
    private val templateRepository:      EventTemplateRepository by inject()
    private val gameContentRepository:   GameContentRepository   by inject()
    private val tokenStorage:            TokenStorage            by inject()

    override suspend fun doWork(): Result = try {
        val authenticated = tokenStorage.hasTokens()

        if (authenticated) {
            runCatching { templateRepository.pushPendingChanges() }
            runCatching { eventRepository.pushPendingChanges() }
        }
        supervisorScope {
            launch { runCatching { gameContentRepository.syncFromServer() } }

            if (authenticated) {
                launch { runCatching { templateRepository.syncFromServer() } }
                launch { runCatching { eventRepository.syncFromServer() } }
            }
        }

        Result.success()
    } catch (e: Exception) {
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    companion object {
        const val WORK_NAME_ONE_SHOT = "sync_one_shot"
        const val WORK_NAME_PERIODIC = "sync_periodic"
    }
}

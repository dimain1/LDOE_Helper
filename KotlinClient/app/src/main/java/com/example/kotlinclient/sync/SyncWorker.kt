package com.example.kotlinclient.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Фоновый воркер (WorkManager) — выполняется при наличии сети.
 *
 * Порядок операций важен для корректной синхронизации FK:
 *   1. Шаблоны (EventTemplate) — создаём первыми, т.к. события ссылаются на их serverId
 *   2. Затем события (Event)
 *   3. GameContent — pull only (создаётся только через ADMIN на сервере)
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val eventRepository:    EventRepository    by inject()
    private val templateRepository: EventTemplateRepository by inject()
    private val gameContentRepository: GameContentRepository by inject()

    override suspend fun doWork(): Result = try {
        // Push: порядок важен — шаблоны раньше событий (события ссылаются на server_id шаблонов)
        templateRepository.pushPendingChanges()
        eventRepository.pushPendingChanges()

        // Pull: независимые источники параллельно; supervisorScope — сбой одного не отменяет другие
        supervisorScope {
            launch { runCatching { templateRepository.syncFromServer() } }
            launch { runCatching { eventRepository.syncFromServer() } }
            launch { runCatching { gameContentRepository.syncFromServer() } }
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

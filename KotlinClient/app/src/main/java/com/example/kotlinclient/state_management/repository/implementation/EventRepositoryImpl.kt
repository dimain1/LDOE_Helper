package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toEntity
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.local_cache.entity.SyncStatus
import com.example.kotlinclient.api_client.ApiService
import com.example.kotlinclient.api_client.dto.EventCreateRequest
import com.example.kotlinclient.api_client.dto.EventUpdateRequest
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EventRepositoryImpl(
    database: AppDatabase,
    private val session: UserSessionProvider,
    private val api: ApiService
) : EventRepository {

    private val eventDao = database.EventDao()
    private val fmt = DateTimeFormatter.ISO_INSTANT

    // ── UI ────────────────────────────────────────────────────────────────────

    override fun getAllEventsUpcomingWithTemplate(): Flow<List<Event>> =
        session.pipe { id ->
            eventDao.getAllEventsUpcomingWithTemplate(id, Instant.now().toEpochMilli())
                .map { list -> list.map { it.toModel() } }
        }

    override fun getAllEventsWithTemplate(): Flow<List<Event>> =
        session.pipe { id ->
            eventDao.getAllEventsWithTemplate(id)
                .map { list -> list.map { it.toModel() } }
        }

    // ── Мутации (optimistic: сначала Room, потом сервер) ─────────────────────

    override suspend fun addEvent(event: Event): Long {
        val userId = session.requireId()
        val entity = event.toEntity().copy(
            userId = userId,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        val localId = eventDao.addEvent(entity)

        try {
            val dto = api.createEvent(entity.toCreateRequest())
            eventDao.confirmCreated(localId, dto.id)
        } catch (_: Exception) {
            // Остаётся PENDING_CREATE — SyncWorker отправит при следующем подключении
        }
        return localId
    }

    override suspend fun updateEvent(event: Event) {
        val userId = session.requireId()
        val entity = event.toEntity().copy(
            userId = userId,
            syncStatus = SyncStatus.PENDING_UPDATE
        )
        eventDao.updateEvent(entity)

        val serverId = entity.serverId ?: return
        try {
            api.updateEvent(serverId, entity.toUpdateRequest())
            eventDao.confirmUpdated(entity.id!!)
        } catch (_: Exception) { }
    }

    override suspend fun deleteEventById(id: Long) {
        val userId = session.requireId()
        eventDao.markDeleted(userId, id)

        // Если есть serverId — удалить на сервере, потом физически удалить из Room
        val pending = eventDao.getPendingDelete(userId)
        pending.filter { it.id == id }.forEach { entity ->
            try {
                entity.serverId?.let { api.deleteEvent(it) }
                eventDao.deleteById(entity.id!!)
            } catch (_: Exception) {
                // остаётся PENDING_DELETE, SyncWorker удалит позже
            }
        }
    }

    // ── Синхронизация ─────────────────────────────────────────────────────────

    override suspend fun pushPendingChanges() {
        val userId = session.requireId()

        // CREATE
        eventDao.getPendingCreate(userId).forEach { entity ->
            try {
                val dto = api.createEvent(entity.toCreateRequest())
                eventDao.confirmCreated(entity.id!!, dto.id)
            } catch (_: Exception) { }
        }

        // UPDATE
        eventDao.getPendingUpdate(userId).forEach { entity ->
            val serverId = entity.serverId ?: return@forEach
            try {
                api.updateEvent(serverId, entity.toUpdateRequest())
                eventDao.confirmUpdated(entity.id!!)
            } catch (_: Exception) { }
        }

        // DELETE
        eventDao.getPendingDelete(userId).forEach { entity ->
            val serverId = entity.serverId ?: return@forEach
            try {
                api.deleteEvent(serverId)
                eventDao.deleteById(entity.id!!)
            } catch (_: Exception) { }
        }
    }

    override suspend fun syncFromServer() {
        val userId = session.requireId()
        val serverEvents = api.getEvents()

        serverEvents.forEach { dto ->
            val localId = eventDao.getLocalIdByServerId(dto.id, userId)
            if (localId == null) {
                // Новое событие с сервера — вставляем
                val templateLocalId = dto.templateId?.let { serverId ->
                    // templateId из сервера может отличаться от localId — ищем по serverId
                    null  // заглушка; реальный маппинг делается в EventWithUserAndTemplate
                }
                eventDao.addEvent(
                    com.example.kotlinclient.local_cache.entity.EventEntity(
                        serverId = dto.id,
                        syncStatus = SyncStatus.SYNCED,
                        templateId = templateLocalId,
                        userId = userId,
                        name = dto.name,
                        description = dto.description,
                        imageUrl = dto.imageUrl,
                        startTime = Instant.parse(dto.startTime),
                        endTime = Instant.parse(dto.endTime)
                    )
                )
            } else {
                // Обновляем существующее
                eventDao.updateByServerId(
                    serverId = dto.id,
                    userId = userId,
                    name = dto.name,
                    description = dto.description,
                    imageUrl = dto.imageUrl,
                    startTime = Instant.parse(dto.startTime).toEpochMilli(),
                    endTime = Instant.parse(dto.endTime).toEpochMilli()
                )
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun com.example.kotlinclient.local_cache.entity.EventEntity.toCreateRequest() =
        EventCreateRequest(
            templateId = serverId,   // NOTE: это local templateId → нужно маппить в serverId шаблона
            name = name,
            description = description,
            imageUrl = imageUrl,
            startTime = fmt.format(startTime),
            endTime = fmt.format(endTime)
        )

    private fun com.example.kotlinclient.local_cache.entity.EventEntity.toUpdateRequest() =
        EventUpdateRequest(
            name = name,
            description = description,
            imageUrl = imageUrl,
            startTime = fmt.format(startTime),
            endTime = fmt.format(endTime)
        )
}

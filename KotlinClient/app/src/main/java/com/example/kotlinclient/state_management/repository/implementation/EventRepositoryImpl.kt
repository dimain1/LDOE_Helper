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
import java.time.format.DateTimeFormatter

class EventRepositoryImpl(
    database: AppDatabase,
    private val session: UserSessionProvider,
    private val api: ApiService
) : EventRepository {

    private val eventDao = database.EventDao()
    private val templateDao = database.EventTemplateDao()
    private val fmt = DateTimeFormatter.ISO_INSTANT

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

    override suspend fun addEvent(event: Event): Long {
        val userId = session.requireId()
        val entity = event.toEntity().copy(
            userId = userId,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        val localId = eventDao.addEvent(entity)

        try {
            val templateServerId = entity.templateId?.let { templateDao.getServerIdByLocalId(it) }
            val dto = api.createEvent(entity.toCreateRequest(templateServerId))
            eventDao.confirmCreated(localId, dto.id)
        } catch (_: Exception) {
            // Остаётся PENDING_CREATE — SyncWorker отправит при следующем подключении
        }
        return localId
    }

    override suspend fun updateEvent(event: Event) {
        val userId = session.requireId()
        val entity = event.toEntity().copy(userId = userId)

        // Сохраняем локально, не трогая server_id в БД
        eventDao.updateEvent(
            id = entity.id!!,
            name = entity.name,
            description = entity.description,
            imageUrl = entity.imageUrl,
            startTime = entity.startTime.toEpochMilli(),
            endTime = entity.endTime.toEpochMilli()
        )

        // Получаем serverId из БД (toEntity() его не несёт)
        val serverId = eventDao.getServerIdByLocalId(entity.id!!) ?: return
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

    override suspend fun pushPendingChanges() {
        val userId = session.requireId()

        // CREATE
        eventDao.getPendingCreate(userId).forEach { entity ->
            try {
                val templateServerId = entity.templateId?.let { templateDao.getServerIdByLocalId(it) }
                val dto = api.createEvent(entity.toCreateRequest(templateServerId))
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
                val templateLocalId = dto.templateId?.let { templateServerId ->
                    templateDao.getLocalIdByServerId(templateServerId, userId)
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

    private fun com.example.kotlinclient.local_cache.entity.EventEntity.toCreateRequest(templateServerId: Long?) =
        EventCreateRequest(
            templateId = templateServerId,
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

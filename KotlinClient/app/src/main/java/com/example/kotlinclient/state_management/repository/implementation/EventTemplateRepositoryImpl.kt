package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toEntity
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.local_cache.entity.EventTemplateEntity
import com.example.kotlinclient.local_cache.entity.SyncStatus
import com.example.kotlinclient.api_client.ApiService
import com.example.kotlinclient.api_client.dto.EventTemplateCreateRequest
import com.example.kotlinclient.api_client.dto.EventTemplateUpdateRequest
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson

class EventTemplateRepositoryImpl(
    database: AppDatabase,
    private val session: UserSessionProvider,
    private val api: ApiService
) : EventTemplateRepository {

    private val templateDao = database.EventTemplateDao()
    private val gson = Gson()

    // ── UI ────────────────────────────────────────────────────────────────────

    override fun getAllTemplateWithUser(): Flow<List<EventTemplate>> =
        session.pipe { id ->
            templateDao.getAllEventTemplateWithUser(id)
                .map { list -> list.map { it.toModel() } }
        }

    // ── Мутации ───────────────────────────────────────────────────────────────

    override suspend fun createTemplate(template: EventTemplate) {
        val userId = session.requireId()
        val entity = template.toEntity().copy(
            creatorId = userId,
            syncStatus = SyncStatus.PENDING_CREATE
        )
        val localId = templateDao.createTemplate(entity)

        try {
            val requestBody = gson.toJson(
                EventTemplateCreateRequest(
                    name = entity.name,
                    description = entity.description,
                    duration = entity.duration
                )
            ).toRequestBody("application/json".toMediaType())

            val dto = api.createTemplate(requestBody, null)  // image upload отдельно
            templateDao.confirmCreated(localId, dto.id)
        } catch (_: Exception) { }
    }

    override suspend fun updateTemplate(template: EventTemplate) {
        val userId = session.requireId()
        val entity = template.toEntity().copy(
            creatorId = userId,
            syncStatus = SyncStatus.PENDING_UPDATE
        )
        templateDao.updateTemplate(entity)

        val serverId = entity.serverId ?: return
        try {
            val requestBody = gson.toJson(
                EventTemplateUpdateRequest(
                    name = entity.name,
                    description = entity.description,
                    duration = entity.duration
                )
            ).toRequestBody("application/json".toMediaType())

            api.updateTemplate(serverId, requestBody, null)
            templateDao.confirmUpdated(entity.id!!)
        } catch (_: Exception) { }
    }

    override suspend fun deleteTemplateById(id: Long) {
        val userId = session.requireId()
        templateDao.markDeleted(userId, id)

        templateDao.getPendingDelete(userId)
            .filter { it.id == id }
            .forEach { entity ->
                try {
                    entity.serverId?.let { api.deleteTemplate(it) }
                    templateDao.deleteById(entity.id!!)
                } catch (_: Exception) { }
            }
    }

    // ── Синхронизация ─────────────────────────────────────────────────────────

    override suspend fun pushPendingChanges() {
        val userId = session.requireId()

        templateDao.getPendingCreate(userId).forEach { entity ->
            try {
                val body = gson.toJson(
                    EventTemplateCreateRequest(entity.name, entity.description, entity.duration)
                ).toRequestBody("application/json".toMediaType())
                val dto = api.createTemplate(body, null)
                templateDao.confirmCreated(entity.id!!, dto.id)
            } catch (_: Exception) { }
        }

        templateDao.getPendingUpdate(userId).forEach { entity ->
            val serverId = entity.serverId ?: return@forEach
            try {
                val body = gson.toJson(
                    EventTemplateUpdateRequest(entity.name, entity.description, entity.duration)
                ).toRequestBody("application/json".toMediaType())
                api.updateTemplate(serverId, body, null)
                templateDao.confirmUpdated(entity.id!!)
            } catch (_: Exception) { }
        }

        templateDao.getPendingDelete(userId).forEach { entity ->
            val serverId = entity.serverId ?: return@forEach
            try {
                api.deleteTemplate(serverId)
                templateDao.deleteById(entity.id!!)
            } catch (_: Exception) { }
        }
    }

    override suspend fun syncFromServer() {
        val userId = session.requireId()
        val serverTemplates = api.getTemplates()

        serverTemplates.forEach { dto ->
            val localId = templateDao.getLocalIdByServerId(dto.id, userId)
            if (localId == null) {
                templateDao.createTemplate(
                    EventTemplateEntity(
                        serverId = dto.id,
                        syncStatus = SyncStatus.SYNCED,
                        creatorId = userId,
                        name = dto.name,
                        description = dto.description,
                        imageUrl = dto.imageUrl,
                        duration = dto.duration
                    )
                )
            } else {
                templateDao.updateByServerId(
                    serverId = dto.id,
                    userId = userId,
                    name = dto.name,
                    description = dto.description,
                    imageUrl = dto.imageUrl,
                    duration = dto.duration
                )
            }
        }
    }
}

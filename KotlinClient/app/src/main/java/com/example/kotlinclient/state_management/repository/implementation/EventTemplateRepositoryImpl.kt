package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.api_client.NetworkConfig
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
import com.example.kotlinclient.state_management.utility.ImageStorageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.io.File

class EventTemplateRepositoryImpl(
    database: AppDatabase,
    private val session: UserSessionProvider,
    private val api: ApiService,
    private val imageStorageManager: ImageStorageManager
) : EventTemplateRepository {

    private val templateDao = database.EventTemplateDao()
    private val gson = Gson()

    private fun buildImagePart(localPath: String?): MultipartBody.Part? {
        if (localPath.isNullOrBlank()) return null
        val file = File(localPath)
        if (!file.exists()) return null
        val requestFile = file.asRequestBody("image/*".toMediaType())
        return MultipartBody.Part.createFormData("image", file.name, requestFile)
    }

    private suspend fun uploadImageIfPresent(
        localId: Long,
        serverId: Long,
        localImagePath: String?,
        name: String,
        description: String?,
        duration: Long
    ) {
        val imagePart = buildImagePart(localImagePath) ?: run {
            if (localImagePath != null) templateDao.confirmImageUploaded(localId, null)
            return
        }
        try {
            val body = gson.toJson(
                EventTemplateUpdateRequest(name, description, duration)
            ).toRequestBody("application/json".toMediaType())
            val dto = api.updateTemplate(serverId, body, imagePart)
            templateDao.confirmImageUploaded(localId, dto.imageUrl)
        } catch (_: Exception) { }
    }

    override fun getAllTemplateWithUser(): Flow<List<EventTemplate>> =
        session.pipe { id ->
            templateDao.getAllEventTemplateWithUser(id)
                .map { list -> list.map { it.toModel() } }
        }


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

            // Phase 1: отправляем данные без картинки — шаблон сразу появляется в UI
            val dto = api.createTemplate(requestBody, null)
            templateDao.confirmCreated(localId, dto.id)

            // Phase 2: загружаем картинку отдельно (не блокирует появление шаблона)
            uploadImageIfPresent(localId, dto.id, entity.localImagePath, entity.name, entity.description, entity.duration)
        } catch (_: Exception) { }
    }

    override suspend fun updateTemplate(template: EventTemplate) {
        val userId = session.requireId()
        val entity = template.toEntity().copy(creatorId = userId)

        // Сохраняем изменения локально, не трогая server_id в БД
        templateDao.updateTemplate(entity.id!!, entity.name, entity.description, entity.imageUrl, entity.localImagePath, entity.duration)

        // Получаем serverId из БД (toEntity() его не несёт)
        val serverId = templateDao.getServerIdByLocalId(entity.id!!) ?: return
        try {
            val requestBody = gson.toJson(
                EventTemplateUpdateRequest(
                    name = entity.name,
                    description = entity.description,
                    duration = entity.duration
                )
            ).toRequestBody("application/json".toMediaType())

            // Phase 1: данные без картинки
            api.updateTemplate(serverId, requestBody, null)
            templateDao.confirmUpdated(entity.id!!)

            // Phase 2: картинка отдельно
            uploadImageIfPresent(entity.id!!, serverId, entity.localImagePath, entity.name, entity.description, entity.duration)
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

    override suspend fun pushPendingChanges() {
        val userId = session.requireId()

        // Phase 1: отправляем все данные без картинок — шаблоны появляются в UI немедленно
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

        // Phase 2: загружаем картинки для всех SYNCED-шаблонов с ожидающим локальным файлом
        templateDao.getSyncedWithLocalImage(userId).forEach { entity ->
            uploadImageIfPresent(entity.id!!, entity.serverId!!, entity.localImagePath, entity.name, entity.description, entity.duration)
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
                // Новый шаблон с сервера — создаём, затем скачиваем картинку
                val newLocalId = templateDao.createTemplate(
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
                downloadAndSaveTemplateImage(newLocalId, dto.imageUrl)
            } else {
                // Существующий шаблон — обновляем метаданные (local_image_path НЕ трогаем)
                templateDao.updateByServerId(
                    serverId = dto.id,
                    userId = userId,
                    name = dto.name,
                    description = dto.description,
                    imageUrl = dto.imageUrl,
                    duration = dto.duration
                )
                // Скачиваем картинку только если локального файла нет
                val existingLocalPath = templateDao.getLocalImagePath(localId)
                val hasValidLocalFile = existingLocalPath != null && File(existingLocalPath).exists()
                if (!hasValidLocalFile) {
                    downloadAndSaveTemplateImage(localId, dto.imageUrl)
                }
            }
        }
    }

    private suspend fun downloadAndSaveTemplateImage(localId: Long, imageUrl: String?) {
        if (imageUrl == null) return
        val fullUrl = NetworkConfig.imageUrl(imageUrl) ?: return
        imageStorageManager.downloadFromUrl(fullUrl)?.let { localPath ->
            templateDao.setLocalImagePath(localId, localPath)
        }
    }
}

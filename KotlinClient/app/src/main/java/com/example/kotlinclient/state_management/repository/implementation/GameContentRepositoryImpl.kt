package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.api_client.NetworkConfig
import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.local_cache.entity.ContentTypeEntity
import com.example.kotlinclient.local_cache.entity.GameContentEntity
import com.example.kotlinclient.local_cache.entity.GameContentTypeCrossRef
import com.example.kotlinclient.local_cache.entity.UserPinnedGameContentCrossRef
import com.example.kotlinclient.api_client.ApiService
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import com.example.kotlinclient.state_management.utility.ImageStorageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class GameContentRepositoryImpl(
    database: AppDatabase,
    private val session: UserSessionProvider,
    private val api: ApiService,
    private val imageStorageManager: ImageStorageManager
) : GameContentRepository {

    private val gameContentDao = database.GameContentDao()

    // ── UI ────────────────────────────────────────────────────────────────────

    override fun getPinnedContent(): Flow<List<GameContent>> =
        session.pipe { id ->
            gameContentDao.getPinnedContent(id).map { list -> list.map { it.toModel() } }
        }

    override fun getFilteredContent(query: String, typeId: Long?): Flow<List<GameContent>> =
        session.pipe { id ->
            gameContentDao.getFilteredContent(id, query, typeId?.takeIf { it != 0L })
                .map { list -> list.map { it.toModel() } }
        }

    // ── Мутации ───────────────────────────────────────────────────────────────

    override suspend fun pinContent(contentId: Long) {
        val userId = session.requireId()
        gameContentDao.pinContent(UserPinnedGameContentCrossRef(userId, contentId))
        runCatching { api.pinContent(contentId) }
    }

    override suspend fun unpinContent(contentId: Long) {
        val userId = session.requireId()
        gameContentDao.unpinContent(userId, contentId)
        runCatching { api.unpinContent(contentId) }
    }

    // ── Синхронизация ─────────────────────────────────────────────────────────

    override suspend fun syncFromServer() {
        val userId = session.requireId()

        // Типы — независимо от контента
        try {
            val allTypes = api.getContentTypes()
            gameContentDao.upsertTypes(allTypes.map { ContentTypeEntity(id = it.id, name = it.name) })
        } catch (_: Exception) { }

        val dtos = try { api.getContent() } catch (_: Exception) { return }

        // Удаляем контент которого нет на сервере
        val serverIds = dtos.map { it.id }
        if (serverIds.isNotEmpty()) gameContentDao.deleteNotIn(serverIds)

        dtos.forEach { dto ->
            // Сохраняем локальный путь к картинке если файл ещё существует
            val existingLocalPath = gameContentDao.getLocalImagePath(dto.id)
                ?.takeIf { File(it).exists() }

            gameContentDao.upsertContent(
                GameContentEntity(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    imageUrl = dto.imageUrl,
                    localImagePath = existingLocalPath,   // сохраняем — не затираем
                    attributes = dto.attributes
                )
            )

            // Скачиваем картинку если локальной копии нет
            if (existingLocalPath == null && dto.imageUrl != null) {
                val fullUrl = NetworkConfig.imageUrl(dto.imageUrl)
                if (fullUrl != null) {
                    imageStorageManager.downloadFromUrl(fullUrl)?.let { localPath ->
                        gameContentDao.updateLocalImagePath(dto.id, localPath)
                    }
                }
            }

            // Типы контента
            val typeEntities = dto.types.map { ContentTypeEntity(id = it.id, name = it.name) }
            gameContentDao.upsertTypes(typeEntities)
            gameContentDao.clearTypeRefs(dto.id)
            gameContentDao.upsertTypeRefs(
                dto.types.map { GameContentTypeCrossRef(gameContentId = dto.id, contentTypeId = it.id) }
            )
        }

        // Пины
        gameContentDao.clearAllPins(userId)
        dtos.filter { it.pinned }.forEach { dto ->
            gameContentDao.pinContent(UserPinnedGameContentCrossRef(userId, dto.id))
        }
    }
}

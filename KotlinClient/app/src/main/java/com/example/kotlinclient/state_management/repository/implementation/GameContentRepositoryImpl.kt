package com.example.kotlinclient.state_management.repository.implementation

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameContentRepositoryImpl(
    database: AppDatabase,
    private val session: UserSessionProvider,
    private val api: ApiService
) : GameContentRepository {

    private val gameContentDao = database.GameContentDao()

    // ── UI ────────────────────────────────────────────────────────────────────

    override fun getPinnedContent(): Flow<List<GameContent>> =
        session.pipe { id ->
            gameContentDao.getPinnedContent(id).map { list -> list.map { it.toModel() } }
        }

    override fun getFilteredContent(query: String, typeId: Long?): Flow<List<GameContent>> =
        session.pipe { id ->
            gameContentDao.getFilteredContent(id, query, typeId)
                .map { list -> list.map { it.toModel() } }
        }

    // ── Мутации (pin/unpin: оптимистично в Room + сервер) ────────────────────

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
        val dtos = api.getContent()

        // 1. Удаляем контент которого нет на сервере
        val serverIds = dtos.map { it.id }
        if (serverIds.isNotEmpty()) {
            gameContentDao.deleteNotIn(serverIds)
        }

        // 2. Upsert контента + типов + cross-refs
        dtos.forEach { dto ->
            gameContentDao.upsertContent(
                GameContentEntity(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    imageUrl = dto.imageUrl,
                    attributes = dto.attributes
                )
            )
            val typeEntities = dto.types.map { ContentTypeEntity(id = it.id, name = it.name) }
            gameContentDao.upsertTypes(typeEntities)

            gameContentDao.clearTypeRefs(dto.id)
            gameContentDao.upsertTypeRefs(
                dto.types.map { GameContentTypeCrossRef(gameContentId = dto.id, contentTypeId = it.id) }
            )
        }

        // 3. Синхронизируем pinned: очищаем локальные пины, восстанавливаем с сервера
        gameContentDao.clearAllPins(userId)
        dtos.filter { it.pinned }.forEach { dto ->
            gameContentDao.pinContent(UserPinnedGameContentCrossRef(userId, dto.id))
        }
    }
}

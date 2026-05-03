package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.local_cache.entity.UserPinnedGameContentCrossRef
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.state_management.repository.UserSession
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameContentRepositoryImpl(
    val database: AppDatabase,
    val session: UserSession
) : GameContentRepository
{
    val gameContentDao = database.GameContentDao()


    override fun getFilteredContent(query: String, typeId: Long): Flow<List<GameContent>> {
        return session.pipe {  id -> gameContentDao.getFilteredContent(id, query, typeId).map {list -> list.map { content -> content.toModel()}} }
    }

    override suspend fun pinContent(contentId: Long) {
        gameContentDao.pinContent(UserPinnedGameContentCrossRef(session.requireId(), contentId))
    }

    override suspend fun unpinContent(contentId: Long) {
        gameContentDao.unpinContent(session.requireId(), contentId)
    }

    override fun getPinnedContent(): Flow<List<GameContent>> {
        return session.pipe { id -> gameContentDao.getPinnedContent(id).map { list -> list.map { content -> content.toModel() } } }
    }


}
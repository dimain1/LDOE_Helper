package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameContentRepositoryImpl(
    val database: AppDatabase
) : GameContentRepository
{
    val gameContentDao = database.GameContentDao()

    override fun getAllContent(): Flow<List<GameContent>> {
        return gameContentDao.getAllContent().map {list -> list.map { content -> content.toModel()}}
    }

    override fun getFilteredContent(query: String, typeId: Long): Flow<List<GameContent>> {
        return gameContentDao.getFilteredContent(query, typeId).map {list -> list.map { content -> content.toModel()}}
    }

    override fun getPinnedContent(): Flow<List<GameContent>> {
        return gameContentDao.getPinnedContent().map { list -> list.map { content -> content.toModel() } }
    }

    override suspend fun changeContentPin(id: Long, pinStatus: Boolean) {
        gameContentDao.updateContentPin(id, pinStatus)
    }
}
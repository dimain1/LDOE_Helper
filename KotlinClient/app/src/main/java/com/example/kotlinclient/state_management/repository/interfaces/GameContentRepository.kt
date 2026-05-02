package com.example.kotlinclient.state_management.repository.interfaces

import com.example.kotlinclient.state_management.entity.GameContent
import kotlinx.coroutines.flow.Flow

interface GameContentRepository {

    fun getAllContent(): Flow<List<GameContent>>

    fun getFilteredContent(query: String, typeId: Long): Flow<List<GameContent>>

    fun getPinnedContent(): Flow<List<GameContent>>

    suspend fun changeContentPin(id: Long, pinStatus: Boolean)

}
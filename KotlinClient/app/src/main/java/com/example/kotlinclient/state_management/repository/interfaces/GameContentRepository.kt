package com.example.kotlinclient.state_management.repository.interfaces

import com.example.kotlinclient.state_management.entity.GameContent
import kotlinx.coroutines.flow.Flow

interface GameContentRepository {

    // ── UI (Room-first) ───────────────────────────────────────────────────────

    fun getPinnedContent(): Flow<List<GameContent>>

    fun getFilteredContent(query: String, typeId: Long?): Flow<List<GameContent>>

    // ── Мутации ───────────────────────────────────────────────────────────────

    suspend fun pinContent(contentId: Long)

    suspend fun unpinContent(contentId: Long)

    // ── Синхронизация ─────────────────────────────────────────────────────────

    /**
     * Загружает весь контент с сервера, выполняет upsert в Room.
     * Восстанавливает флаги pinned согласно серверному ответу.
     */
    suspend fun syncFromServer()
}

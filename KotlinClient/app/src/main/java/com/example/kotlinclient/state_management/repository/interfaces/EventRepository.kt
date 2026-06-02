package com.example.kotlinclient.state_management.repository.interfaces

import com.example.kotlinclient.state_management.entity.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {

    // ── UI (Room-first, всегда реактивны) ────────────────────────────────────

    fun getAllEventsUpcomingWithTemplate(): Flow<List<Event>>

    fun getAllEventsWithTemplate(): Flow<List<Event>>

    // ── Мутации (сначала Room, потом сервер) ─────────────────────────────────

    suspend fun addEvent(event: Event): Long

    suspend fun updateEvent(event: Event)

    suspend fun deleteEventById(id: Long)

    // ── Синхронизация с сервером ─────────────────────────────────────────────

    /** Отправить на сервер все локальные изменения (PENDING_*). */
    suspend fun pushPendingChanges()

    /** Получить события с сервера и обновить Room. */
    suspend fun syncFromServer()
}

package com.example.kotlinclient.state_management.repository.interfaces

import com.example.kotlinclient.state_management.entity.EventTemplate
import kotlinx.coroutines.flow.Flow

interface EventTemplateRepository {

    // ── UI (Room-first) ───────────────────────────────────────────────────────

    fun getAllTemplateWithUser(): Flow<List<EventTemplate>>

    // ── Мутации ───────────────────────────────────────────────────────────────

    suspend fun createTemplate(template: EventTemplate)

    suspend fun updateTemplate(template: EventTemplate)

    suspend fun deleteTemplateById(id: Long)

    // ── Синхронизация ─────────────────────────────────────────────────────────

    suspend fun pushPendingChanges()

    suspend fun syncFromServer()
}

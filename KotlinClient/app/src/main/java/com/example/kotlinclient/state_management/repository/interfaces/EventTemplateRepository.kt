package com.example.kotlinclient.state_management.repository.interfaces

import com.example.kotlinclient.state_management.entity.EventTemplate
import kotlinx.coroutines.flow.Flow

interface EventTemplateRepository {

    fun getAllTemplateWithUser(): Flow<List<EventTemplate>>


    suspend fun createTemplate(template: EventTemplate)

    suspend fun updateTemplate(template: EventTemplate)

    suspend fun deleteTemplateById(id: Long)

    suspend fun pushPendingChanges()

    suspend fun syncFromServer()
}

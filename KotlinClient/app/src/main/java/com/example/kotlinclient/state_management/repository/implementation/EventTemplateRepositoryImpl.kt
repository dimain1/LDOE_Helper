package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventTemplateRepositoryImpl(
    val database: AppDatabase
): EventTemplateRepository {

    val eventTemplateDao = database.EventTemplateDao()

    override fun getAllTemplateWithUser(): Flow<List<EventTemplate>> {
        return eventTemplateDao.getAllEventTemplateWithUser().map { list -> list.map { template -> template.toModel()   } }
    }

    override suspend fun deleteTemplateById(id: Long) {
        eventTemplateDao.deleteTemplateById(id)
    }
}
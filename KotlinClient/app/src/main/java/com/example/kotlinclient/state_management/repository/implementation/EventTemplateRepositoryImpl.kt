package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toEntity
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.UserSession
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventTemplateRepositoryImpl(
    database: AppDatabase,
    val session: UserSession
): EventTemplateRepository {

    val eventTemplateDao = database.EventTemplateDao()

    override fun getAllTemplateWithUser(): Flow<List<EventTemplate>> {
        return session.pipe {id -> eventTemplateDao.getAllEventTemplateWithUser(id).map { list -> list.map { template -> template.toModel()   } } }
    }

    override suspend fun deleteTemplateById(id: Long) {

        eventTemplateDao.deleteTemplateById(session.requireId(), id)
    }

    override suspend fun createTemplate(template: EventTemplate) {
        eventTemplateDao.createTemplate(template = template.toEntity())
    }

    override suspend fun updateTemplate(template: EventTemplate) {
        eventTemplateDao.updateTemplate(template=template.toEntity())
    }


}
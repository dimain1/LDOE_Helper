package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.UserSession
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class EventTemplateRepositoryImpl(
    val database: AppDatabase,
    val session: UserSession
): EventTemplateRepository {

    val eventTemplateDao = database.EventTemplateDao()

    override fun getAllTemplateWithUser(): Flow<List<EventTemplate>> {
        return session.pipe {id -> eventTemplateDao.getAllEventTemplateWithUser(id).map { list -> list.map { template -> template.toModel()   } } }
    }

    override suspend fun deleteTemplateById(id: Long) {

        eventTemplateDao.deleteTemplateById(session.requireId(), id)
    }
}
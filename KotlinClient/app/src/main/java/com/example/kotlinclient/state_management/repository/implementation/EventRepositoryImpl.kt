package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toEntity
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class EventRepositoryImpl(
    database: AppDatabase,
    val session: UserSessionProvider
) : EventRepository
{

    val eventDao = database.EventDao()

    override fun getAllEventsUpcomingWithTemplate(): Flow<List<Event>> {
        return session.pipe { id  -> eventDao.getAllEventsUpcomingWithTemplate(id,
            Instant.now().toEpochMilli()).map { list ->  list.map { event -> event.toModel() } }}
    }

    override fun getAllEventsWithTemplate(): Flow<List<Event>> {
        return session.pipe { id ->
            eventDao.getAllEventsWithTemplate(id)
                .map { list -> list.map { event -> event.toModel() } }
        }
    }

    override suspend fun deleteEventById(id: Long) {
        eventDao.deleteEventById(session.requireId(), id)
    }

    override suspend fun addEvent(event: Event) {
        eventDao.addEvent(event.toEntity())
    }

    override suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event.toEntity())
    }
}
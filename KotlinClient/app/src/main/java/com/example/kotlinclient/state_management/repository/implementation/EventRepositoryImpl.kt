package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class EventRepositoryImpl(
    val database: AppDatabase
) : EventRepository
{

    val eventDao = database.EventDao()

    override fun getAllEventsUpcomingWithTemplate(): Flow<List<Event>> {
        return eventDao.getAllEventsUpcomingWithTemplate().map { list ->  list.map { event -> event.toModel() } }
    }

    override fun getAllEventsWithTemplate(): Flow<List<Event>> {
        return eventDao.getAllEventsWithTemplate().map { list ->  list.map { event -> event.toModel() } }
    }

    override suspend fun deleteEventById(id: Long) {
        eventDao.deleteEventById(id)
    }

}
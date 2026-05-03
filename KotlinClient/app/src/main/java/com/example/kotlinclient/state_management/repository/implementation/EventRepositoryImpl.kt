package com.example.kotlinclient.state_management.repository.implementation

import androidx.compose.foundation.isSystemInDarkTheme
import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.repository.UserSession
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class EventRepositoryImpl(
    val database: AppDatabase,
    val session: UserSession
) : EventRepository
{

    val eventDao = database.EventDao()

    override fun getAllEventsUpcomingWithTemplate(): Flow<List<Event>> {
        return session.pipe { id  -> eventDao.getAllEventsUpcomingWithTemplate(id).map { list ->  list.map { event -> event.toModel() } }}
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

}
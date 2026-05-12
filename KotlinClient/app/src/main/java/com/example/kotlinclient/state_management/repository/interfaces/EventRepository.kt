package com.example.kotlinclient.state_management.repository.interfaces

import com.example.kotlinclient.state_management.entity.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {

    fun getAllEventsUpcomingWithTemplate() : Flow<List<Event>>

    fun getAllEventsWithTemplate() : Flow<List<Event>>

    suspend fun deleteEventById(id: Long)

    suspend fun addEvent(event: Event)

    suspend fun updateEvent(event: Event)
}
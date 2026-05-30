package com.example.kotlinclient.ViewModelTest

import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.ZoneId

class FakeEventRepository : EventRepository {

    val events = MutableStateFlow<List<Event>>(emptyList())

    override fun getAllEventsUpcomingWithTemplate(): Flow<List<Event>> {
        return events.map { list ->
            list.filter {
                it.endTime.atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli() > System.currentTimeMillis() && it.startTime.atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli() <= System.currentTimeMillis()
            }
        }
    }

    override fun getAllEventsWithTemplate(): Flow<List<Event>> {
        return events
    }

    override suspend fun deleteEventById(id: Long) {
        events.update { list ->
            list.filterNot { it.id == id }
        }
    }

    override suspend fun addEvent(event: Event) {
        events.update { list ->
            list.map {
                if (it.id == event.id) event else it
            }
        }
    }

    override suspend fun updateEvent(event: Event) {
        events.update { list ->
            list.map {
                if (it.id == event.id) event else it
            }
        }
    }
}


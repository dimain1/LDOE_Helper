package com.example.kotlinclient.state_management.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(
    val eventRepository: EventRepository,
): ViewModel() {

    val Events: StateFlow<List<Event>> = eventRepository.getAllEventsWithTemplate().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun deleteEventById(id:Long){
        viewModelScope.launch {
            eventRepository.deleteEventById(id)
        }
    }

}
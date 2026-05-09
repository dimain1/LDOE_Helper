package com.example.kotlinclient.state_management.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventUiState(
    val events: List<Event> = emptyList()
)

sealed interface EventAction{
    data class DeleteEvent(val id: Long): EventAction
    data object ClearUiState: EventAction
}

class EventViewModel(
    val eventRepository: EventRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())

    val uiState = _uiState.asStateFlow()

    init{

        eventRepository.getAllEventsWithTemplate().onEach {
            events -> _uiState.update { it.copy(events=events) }
        }.launchIn(viewModelScope)

    }

    fun deleteEvent(id:Long){
        viewModelScope.launch {
            eventRepository.deleteEventById(id)
        }
    }

    fun clearUiState(){
        _uiState.value = EventUiState()
    }

}
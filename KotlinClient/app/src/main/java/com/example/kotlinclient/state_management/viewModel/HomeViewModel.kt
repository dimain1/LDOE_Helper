package com.example.kotlinclient.state_management.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val upcomingEvents: List<Event> = emptyList(),
    val pinnedEntity: List<GameContent> = emptyList()
)

sealed interface HomeAction{
    data class DeleteEvent(val id: Long): HomeAction
    data class TogglePin(val id: Long, val pinStatus: Boolean): HomeAction
    data object ToEvent: HomeAction
    data object ToTemplate: HomeAction
    data object ToInfo: HomeAction
    data object ToSettings: HomeAction
}


class HomeViewModel(
    val gameContentRepository: GameContentRepository,
    val eventRepository: EventRepository
) : ViewModel()
{

    val uiState: StateFlow<HomeUiState> = combine( eventRepository.getAllEventsUpcomingWithTemplate(), gameContentRepository.getPinnedContent() ){
        events, entities -> HomeUiState(upcomingEvents=events, pinnedEntity=entities)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

}


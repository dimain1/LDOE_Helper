package com.example.kotlinclient.state_management.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    val gameContentRepository: GameContentRepository,
    val eventRepository: EventRepository
) : ViewModel()
{

    val upcomingEvents: StateFlow<List<Event>> = eventRepository.getAllEventsUpcomingWithTemplate().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val pinnedEntity: StateFlow<List<GameContent>> = gameContentRepository.getPinnedContent().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )


}
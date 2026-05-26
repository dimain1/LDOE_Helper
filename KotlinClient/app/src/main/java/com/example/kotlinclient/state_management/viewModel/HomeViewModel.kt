package com.example.kotlinclient.state_management.viewModel

import android.content.Context
import android.widget.Toast
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
    data class LaunchOverlay(val context: Context) : HomeAction
    data class ShowEventDetails(val initialData: Event?) : HomeAction
}


class HomeViewModel(
    gameContentRepository: GameContentRepository,
    eventRepository: EventRepository
) : ViewModel()
{

    // region flows

    val uiState: StateFlow<HomeUiState> = combine(
        eventRepository.getAllEventsUpcomingWithTemplate(),
        gameContentRepository.getPinnedContent()
    ) { events, entities ->
        HomeUiState(upcomingEvents = events, pinnedEntity = entities)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    // endregion

    fun onAction(action: HomeAction){
        when(action){
            is HomeAction.LaunchOverlay -> launchOverlay(action.context)
            is HomeAction.DeleteEvent -> TODO()
            is HomeAction.ToEvent -> TODO()
            is HomeAction.ToInfo -> TODO()
            is HomeAction.ToSettings -> TODO()
            is HomeAction.ToTemplate -> TODO()
            is HomeAction.TogglePin -> TODO()
            is HomeAction.ShowEventDetails -> TODO()// Обрабатываються в GraphBuilder
        }
    }

    // region onAction function

    private fun launchOverlay(context: Context){
        Toast.makeText(context,"Launch Overlay", Toast.LENGTH_SHORT).show()
    }

    // endregion

}


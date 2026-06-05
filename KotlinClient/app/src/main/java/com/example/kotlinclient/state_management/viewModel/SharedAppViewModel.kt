package com.example.kotlinclient.state_management.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


data class SharedUiState(
    val darkTheme: Boolean = false,
    val eventDetailsDialogUiState: EventDetailsDialogUiState = EventDetailsDialogUiState(),
    val gameContentDetailsDialogUiState: GameContentDialogUiState = GameContentDialogUiState()
)

data class EventDetailsDialogUiState(
    val showDialog: Boolean = false,
    val initialData: Event? = null
)

data class GameContentDialogUiState(
    val showDialog: Boolean = false,
    val initialData: GameContent? = null
)

sealed interface DialogType {
    data class EventViewDetailsDialog(val initialData: Event?) : DialogType
    data class GameContentViewDetailsDialog(val initialData: GameContent?): DialogType
}

sealed interface SharedAction {
    data class ChangeDialogVisibility(val visible: Boolean, val dialogType: DialogType) : SharedAction
}

class SharedAppViewModel(
    sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    // region flows

        // region  eventDetailsDialog Flows
    val showEventViewDetailsDialog = MutableStateFlow(false)
    val eventInitialData = MutableStateFlow<Event?>(null)

    val eventDetailsDialogUiState: StateFlow<EventDetailsDialogUiState> = combine(
        showEventViewDetailsDialog,
        eventInitialData
    ) { visible, initialData ->
        EventDetailsDialogUiState(visible, initialData)
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        EventDetailsDialogUiState()
    )
        // endregion

        // region gameContentDetailsDialog Flows

    val showGameContentViewDetailsDialog = MutableStateFlow(false)
    val gameContentInitialData = MutableStateFlow<GameContent?>(null)

    val gameContentDetailsDialogUiState: StateFlow<GameContentDialogUiState> = combine(
        showGameContentViewDetailsDialog,
        gameContentInitialData,
    ) { visible, initialData ->
        GameContentDialogUiState(visible,initialData)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameContentDialogUiState())

        // endregion

    val uiState: StateFlow<SharedUiState> = combine(
        sharedPreferencesRepository.observeBoolean("theme", false),
        eventDetailsDialogUiState,
        gameContentDetailsDialogUiState
    )
    { theme, eventDetailsDialogUiState, gameContentDetailsDialogUiState ->
        SharedUiState(theme, eventDetailsDialogUiState, gameContentDetailsDialogUiState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SharedUiState())

    // endregion

    fun onAction(action: SharedAction) {
        when (action) {
            is SharedAction.ChangeDialogVisibility -> changeDialogVisibility(
                action.visible,
                action.dialogType
            )
        }
    }

    // region onAction function

    private fun changeDialogVisibility(visible: Boolean, dialogType: DialogType) {
        when(dialogType){
            is DialogType.EventViewDetailsDialog -> {
                eventInitialData.value = dialogType.initialData
                showEventViewDetailsDialog.value = visible

            }
            is DialogType.GameContentViewDetailsDialog ->{
                gameContentInitialData.value= dialogType.initialData
                showGameContentViewDetailsDialog.value = visible

            }
        }

    }

    // endregion
}
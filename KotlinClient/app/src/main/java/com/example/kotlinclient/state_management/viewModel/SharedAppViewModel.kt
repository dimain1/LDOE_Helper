package com.example.kotlinclient.state_management.viewModel

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update



data class SharedUiState(
    val darkTheme: Boolean = false,
    val eventDetailsDialogUiState: EventDetailsDialogUiState = EventDetailsDialogUiState()
)

data class EventDetailsDialogUiState(
    val showDialog: Boolean = false,
    val initialData: Event? = null
)

sealed interface DialogType {
    data class EventViewDetailsDialog(val initialData: Event?) : DialogType
}

sealed interface SharedAction {
    data class ChangeDialogVisibility(val visible: Boolean, val dialogType: DialogType) : SharedAction
}

class SharedAppViewModel(
    val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    // region flows

        // region  eventDetailsDialog Flows
    val showEventViewDetailsDialog = MutableStateFlow(false)
    val eventInitialData = MutableStateFlow<Event?>(null)

    val _eventDetailsDialogUiState: StateFlow<EventDetailsDialogUiState> = combine(
        showEventViewDetailsDialog,
        eventInitialData
    ) { visible, initialData ->
        EventDetailsDialogUiState(visible, initialData)
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        EventDetailsDialogUiState()
    )
        // endregion
    val _uiState: StateFlow<SharedUiState> = combine(
        sharedPreferencesRepository.observeBoolean("theme", false),
        _eventDetailsDialogUiState
    )
    { theme, eventDetailsDialogUiState ->
        SharedUiState(theme, eventDetailsDialogUiState)
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
                showEventViewDetailsDialog.value = visible
                eventInitialData.value = dialogType.initialData
            }
        }

    }

    // endregion
}
package com.example.kotlinclient.state_management.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventTemplateUiState(
    val templates: List<EventTemplate> = emptyList()
)

sealed interface EventTemplateAction{
    data class DeleteTemplate(val id: Long): EventTemplateAction
}

class EventTemplateViewModel(
    val eventTemplateRepository: EventTemplateRepository
): ViewModel() {

    private val _uiState = MutableStateFlow( EventTemplateUiState())

    val uiState = _uiState.asStateFlow()

    init{

        eventTemplateRepository.getAllTemplateWithUser().onEach {
            templates -> _uiState.update { it.copy(templates=templates) }
        }.launchIn(viewModelScope)

    }

    fun deleteTemplate(id: Long){
        viewModelScope.launch {
            eventTemplateRepository.deleteTemplateById(id)
        }
    }

}
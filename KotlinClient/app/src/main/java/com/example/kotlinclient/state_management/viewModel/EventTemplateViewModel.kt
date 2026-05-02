package com.example.kotlinclient.state_management.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.di.viewModelModule
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventTemplateViewModel(
    val eventTemplateRepository: EventTemplateRepository
): ViewModel() {

    val templates: StateFlow<List<EventTemplate>> = eventTemplateRepository.getAllTemplateWithUser().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun deleteTemplate(id: Long){
        viewModelScope.launch {
            eventTemplateRepository.deleteTemplateById(id)
        }
    }

}
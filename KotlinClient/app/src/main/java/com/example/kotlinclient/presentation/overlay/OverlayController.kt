package com.example.kotlinclient.presentation.overlay

import com.example.kotlinclient.presentation.utility.EventAlarmScheduler
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OverlayController(
    private val repository: EventRepository,
    private val templateRepository: EventTemplateRepository,
    private val alarmScheduler: EventAlarmScheduler,
    private val session: UserSessionProvider
) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow(OverlayUiState())
    val state: StateFlow<OverlayUiState> = _state

    fun expand() {
        _state.value = _state.value.copy(isExpanded = true)
    }

    fun collapse() {
        _state.value = OverlayUiState(isExpanded = false, screen = OverlayScreen.Menu)
    }

    fun openEvents() {
        _state.value = _state.value.copy(screen = OverlayScreen.Events)
    }

    fun openCreate() {
        _state.value = _state.value.copy(screen = OverlayScreen.Create)
    }

    /** Возврат на меню без закрытия оверлея (для кнопки «Back» во вложенных экранах). */
    fun openMenu() {
        _state.value = _state.value.copy(screen = OverlayScreen.Menu)
    }

    fun createEvent(event: Event) {
        scope.launch {
            val savedEvent = event.copy(user = session.user.value)
            val savedEventId = repository.addEvent(savedEvent)
            alarmScheduler.scheduleFinish(savedEvent.copy(id = savedEventId))
            openEvents()
        }
    }

    fun observeEvents(onEach: (List<Event>) -> Unit) {
        scope.launch {
            repository.getAllEventsWithTemplate().collect {
                onEach(it)
            }
        }
    }

    fun observeTemplates(onEach: (List<EventTemplate>) -> Unit) {
        scope.launch {
            templateRepository.getAllTemplateWithUser().collect {
                onEach(it)
            }
        }
    }

    fun clear() {
        scope.cancel()
    }
}

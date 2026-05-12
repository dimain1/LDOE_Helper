package com.example.kotlinclient.state_management.viewModel


import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.UserSession
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.repository.interfaces.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class EventUiState(
    val events: List<Event> = emptyList(),
    val activeDialog: EventDialogType? = null
)

sealed interface EventDialogType{
    data object Create: EventDialogType
    data object Edit: EventDialogType
    data class View(val eventData: Event): EventDialogType
}

sealed interface EventAction {
    data class DeleteEvent(val id: Long) : EventAction
    data object DismissDialog : EventAction
    data class OpenDialog(val dialog: EventDialogType): EventAction

}

data class EventFormFields(

    val id: Long? =  null,
    val name: TextFieldState = TextFieldState(""),
    val description: TextFieldState = TextFieldState(""),
    val startTime: TextFieldState = TextFieldState(""),
    val endTime: TextFieldState = TextFieldState(""),
    val template: EventTemplate? = null,
)

sealed interface EventFormAction{
    data object ClearUiState : EventFormAction
    data class SelectTemplate(val template: EventTemplate): EventFormAction
    data class LoadUiState(val event: Event): EventFormAction
    data object ValidateAndSave: EventFormAction
    data object UpdateEndTime: EventFormAction
}

sealed interface ValidationEvent {
    data object EmptyName : ValidationEvent
    data object InvalidTime : ValidationEvent
    data object SuccessCreate : ValidationEvent
    data object SuccessUpdate: ValidationEvent
}



class EventViewModel(
    val eventRepository: EventRepository,
    val session: UserSession,
) : ViewModel() {

    final val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")

    private val _validationEvents = Channel<ValidationEvent>()
    val validationEvents = _validationEvents.receiveAsFlow()

    private val _uiState = MutableStateFlow(EventUiState())

    val uiState = _uiState.asStateFlow()

    private val _eventFormFields = MutableStateFlow(EventFormFields())

    val eventFormFields = _eventFormFields.asStateFlow()

    private val _selectedTemplate = MutableStateFlow<EventTemplate?>(null)
    private val _selectedId = MutableStateFlow<Long?>(null)

    init {

        eventRepository.getAllEventsWithTemplate().onEach { events ->
            _uiState.update { it.copy(events = events) }
        }.launchIn(viewModelScope)

        _selectedTemplate.onEach { template ->
            _eventFormFields.update { it.copy(template = template) }
                if(template != null) onTemplateSelect()
        }.launchIn(viewModelScope)

        _selectedId.onEach { id ->
            _eventFormFields.update { it.copy(id=id) }
        }.launchIn(viewModelScope)


    }

    fun selectTemplate(template: EventTemplate){
        _selectedTemplate.value = template
    }

    fun insertTemplateName(){
        _eventFormFields.value.name.edit { insert(0, "${_eventFormFields.value.template?.name} - ") }
    }

    fun updateEndTime(){
        if(!_eventFormFields.value.startTime.text.isEmpty()) {
            _eventFormFields.value.endTime.edit {
                replace(
                    0,
                    length,
                    addDuration(
                        _eventFormFields.value.startTime.text.toString(),
                        _eventFormFields.value.template?.duration ?: 0
                    )
                )
            }
        }
    }

    fun onTemplateSelect() {
        insertTemplateName()
        updateEndTime()
    }


    fun getTimeByString(time: String, format: DateTimeFormatter = dateTimeFormat): LocalDateTime{
        return LocalDateTime.parse(time, format)
    }

    fun getStringByTime(time: LocalDateTime, format: DateTimeFormatter=dateTimeFormat): String{
        return time.format(format)
    }

    fun addDuration(startTime: String, duration: Long): String {
        var startTimeInTime =
            getTimeByString(startTime)

        return startTimeInTime.plusSeconds(duration / 1000)
            .format(dateTimeFormat)
    }

    fun validateTime(): Boolean{
        if(_eventFormFields.value.startTime.text.isEmpty() || _eventFormFields.value.endTime.text.isEmpty()) return false
        val startTime = getTimeByString(_eventFormFields.value.startTime.text.toString())
        val endTime = getTimeByString(_eventFormFields.value.endTime.text.toString())

        return startTime.compareTo(endTime) < 0
    }


    private suspend fun isDataValid(state: EventFormFields): Boolean {
        return when {
            state.name.text.isEmpty() -> {
                _validationEvents.send(ValidationEvent.EmptyName)
                false
            }
            !validateTime() -> {
                _validationEvents.send(ValidationEvent.InvalidTime)
                false
            }
            else -> true
        }
    }


    fun saveEvent() {
        val state = _eventFormFields.value

        viewModelScope.launch {
            if (isDataValid(state)) {
                val event = Event(
                    id = state.id, // null для создания, ID для обновления
                    user = session.user.value,
                    template = state.template,
                    name = state.name.text.toString(),
                    description = state.description.text.toString(),
                    image = null,
                    start_time = getTimeByString(state.startTime.text.toString()),
                    end_time = getTimeByString(state.endTime.text.toString())
                )
                if (state.id == null) {
                    eventRepository.addEvent(event)
                    _validationEvents.send(ValidationEvent.SuccessCreate)
                } else {
                    eventRepository.updateEvent(event)
                    _validationEvents.send(ValidationEvent.SuccessUpdate)
                }
            }
        }
    }

    fun loadUiState(event: Event){
        var uiState = _eventFormFields.value

        _selectedTemplate.value = event!!.template
        _selectedId.value = event!!.id
        uiState.name.edit { replace(0, length, event.name.toString()) }
        uiState.description.edit { replace(0, length, event.description.toString()) }
        uiState.startTime.edit { replace(0, length, getStringByTime(event.start_time) ) }
        uiState.endTime.edit { replace(0, length, getStringByTime(event.end_time) ) }
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            eventRepository.deleteEventById(id)
        }
    }

    fun clearUiState() {
        _eventFormFields.value = EventFormFields()
    }

    fun dismissDialog(){
        _uiState.update { it.copy(activeDialog = null) }
    }

    fun openDialog(dialog: EventDialogType){
        _uiState.update { it.copy(activeDialog = dialog) }
    }

}
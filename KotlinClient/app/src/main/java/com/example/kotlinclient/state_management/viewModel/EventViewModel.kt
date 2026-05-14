package com.example.kotlinclient.state_management.viewModel


import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.UserSession
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Состояние экрана событий
data class EventUiState(
    val events: List<Event> = emptyList(),
    val activeDialog: EventDialogType? = null
)

// Запечатанный интерфейс типов основных модальных окон
sealed interface EventDialogType{
    data object Create: EventDialogType
    data object Edit: EventDialogType
    data class View(val eventData: Event): EventDialogType
}

// Запечатанный интерфейс действий на экране событий
sealed interface EventAction {
    data class DeleteEvent(val id: Long) : EventAction
    data object DismissDialog : EventAction
    data class OpenDialog(val dialog: EventDialogType): EventAction

}

// дата класс состояния формы создания
data class EventFormFields(
    val id: Long? =  null,
    val name: TextFieldState = TextFieldState(""),
    val description: TextFieldState = TextFieldState(""),
    val startTime: TextFieldState = TextFieldState(""),
    val endTime: TextFieldState = TextFieldState(""),
    val template: EventTemplate? = null,
)

// Запечатанный интерфейс действий формы создания
sealed interface EventFormAction{
    data object ClearUiState : EventFormAction
    data class SelectTemplateInPicker(val template: EventTemplate): EventFormAction
    data object SelectTemplateInModal: EventFormAction
    data object SyncFormTemplateAndSelectedTemplate: EventFormAction
    data class LoadUiState(val event: Event): EventFormAction
    data object ValidateAndSave: EventFormAction
    data object UpdateEndTime: EventFormAction
}

// Запечатанный интерфейс событий валидации
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

    // Формат представления даты и времени
    final val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")

    // Канал валидации формы создания(изменения) событий
    private val _validationEvents = Channel<ValidationEvent>()
    val validationEvents = _validationEvents.receiveAsFlow()

    // Состояние всего экрана событий
    private val _uiState = MutableStateFlow(EventUiState())

    val uiState = _uiState.asStateFlow()

    // Состояние EventCreateModal
    private val _eventFormFields = MutableStateFlow(EventFormFields())

    val eventFormFields = _eventFormFields.asStateFlow()

    // Поток выбранного шаблона, используется для TemplatePicker
    private val _selectedTemplate = MutableStateFlow<EventTemplate?>(null)
    val selectedTemplate = _selectedTemplate.asStateFlow()
    // Поток id события, используется для редактирования событий
    private val _selectedId = MutableStateFlow<Long?>(null)

    // init блок запуска слушателей
    init {

        // Слушатель изменений событий
        eventRepository.getAllEventsWithTemplate().onEach { events ->
            _uiState.update { it.copy(events = events) }
        }.launchIn(viewModelScope)

        // Слушателель изменений id события
        _selectedId.onEach { id ->
            _eventFormFields.update { it.copy(id=id) }
        }.launchIn(viewModelScope)

    }

    // Поток состояния текущего времени обновляющийся раз в минуту
    val currentTime = flow {
        while (true) {
            emit(LocalDateTime.now())
            Log.d("DEBUG", "current Time ${LocalDateTime.now(ZoneId.systemDefault())}")
            delay(60_000) // Обновляем раз в минуту
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalDateTime.now())

    // Изменяет значения состояния выбранного шаблона
    fun selectTemplateInPicker(template: EventTemplate){
        _selectedTemplate.value = template
    }

    // Устанавливает значение шаблона для события в форме, также применяет соответсвующие операции
    // изменения других полей в форме
    fun selectTemplateInCreate(){
        _eventFormFields.update { it.copy(template = _selectedTemplate.value) }
        onTemplateSelect()
    }
    // Синхронизирует значение выбранного шаблона с шаблоном в состоянии формы, используется при открытии TemplatePicker
    fun syncFormTemplateAndSelected(){
        _selectedTemplate.value = _eventFormFields.value.template
    }

    // Заменяет имя события на шаблон вида "{Template.Name} - "
    fun replaceTemplateName(){
        _eventFormFields.value.name.edit { replace(0, length ,"${_eventFormFields.value.template?.name} - ") }
    }

    // Обновляет время конца события на основе длительности шаблона и времни начала
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

    // Комплексный вызов функций изменения полей ввода при выборе шаблона
    fun onTemplateSelect() {
        replaceTemplateName()
        updateEndTime()
    }


    // Получение времени из строки по формату
    fun getTimeByString(time: String, format: DateTimeFormatter = dateTimeFormat): LocalDateTime{
        return LocalDateTime.parse(time, format)
    }

    // Получение строки из времени по формату
    fun getStringByTime(time: LocalDateTime, format: DateTimeFormatter=dateTimeFormat): String{
        return time.format(format)
    }

    // Добавление длительности к начальному времение, используется для end_time
    fun addDuration(startTime: String, duration: Long): String {
        var startTimeInTime =
            getTimeByString(startTime)

        return startTimeInTime.plusSeconds(duration / 1000)
            .format(dateTimeFormat)
    }

    // Проверка времени на то что начало не позднее конца, и то что они заполнены
    fun validateTime(): Boolean{
        if(_eventFormFields.value.startTime.text.isEmpty() || _eventFormFields.value.endTime.text.isEmpty()) return false
        val startTime = getTimeByString(_eventFormFields.value.startTime.text.toString())
        val endTime = getTimeByString(_eventFormFields.value.endTime.text.toString())

        return startTime.compareTo(endTime) < 0
    }

    // Валидация полей формы
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

    // Сохранение(изменение) события
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

    // Загрузка состояния события для редактирования
    fun loadUiState(event: Event){
        var uiState = _eventFormFields.value

        _selectedTemplate.value = event!!.template
        _selectedId.value = event!!.id
        uiState.name.edit { replace(0, length, event.name.toString()) }
        uiState.description.edit { replace(0, length, event.description.toString()) }
        uiState.startTime.edit { replace(0, length, getStringByTime(event.start_time) ) }
        uiState.endTime.edit { replace(0, length, getStringByTime(event.end_time) ) }
    }

    // Удаление события
    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            eventRepository.deleteEventById(id)
        }
    }

    // Очистка состояния для создания
    fun clearUiState() {
        _eventFormFields.value = EventFormFields()
        Log.d("DEBUG", "Clear State")
    }

    // Закрытие основных диалогов
    fun dismissDialog(){
        _uiState.update { it.copy(activeDialog = null) }
    }

    // Открытие основных диаголов
    fun openDialog(dialog: EventDialogType){
        _uiState.update { it.copy(activeDialog = dialog) }
    }

}
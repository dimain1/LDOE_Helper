package com.example.kotlinclient.state_management.viewModel



import android.content.Context
import android.util.Log
import android.widget.Toast
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

// region EventScreen

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

// endregion

// region EventForm

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
    data class SelectTemplate(val template: EventTemplate?): EventFormAction
    data class LoadUiState(val event: Event): EventFormAction
    data class ValidateAndSave(val context: Context): EventFormAction
    data object UpdateEndTime: EventFormAction
}

// Запечатанный интерфейс событий валидации
sealed interface ValidationEvent {
    data class EmptyName(val context: Context) : ValidationEvent
    data class EmptyTime(val context: Context) : ValidationEvent
    data class InvalidTime(val context: Context) : ValidationEvent
    data class SuccessCreate(val context: Context) : ValidationEvent
    data class SuccessUpdate(val context: Context): ValidationEvent
}

// endregion

class EventViewModel(
    val eventRepository: EventRepository,
    val session: UserSession,
) : ViewModel() {

    // Формат представления даты и времени
    final val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")

    // region flows

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
    // Поток id события, используется для редактирования событий
    private val _selectedId = MutableStateFlow<Long?>(null)

    // endregion

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

        _selectedTemplate.onEach { template ->
            _eventFormFields.update { it.copy(template= template) }
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


    fun onAction(action: EventAction){
        when(action){
            is EventAction.DeleteEvent -> deleteEvent(action.id)
            is EventAction.DismissDialog -> dismissDialog()
            is EventAction.OpenDialog -> openDialog(action.dialog)
        }
    }

    // region onAction function

    // Удаление события
    private fun deleteEvent(id: Long) {
        viewModelScope.launch {
            eventRepository.deleteEventById(id)
        }
    }

    // Закрытие основных диалогов
    private fun dismissDialog(){
        _uiState.update { it.copy(activeDialog = null) }
    }

    // Открытие основных диаголов
    private fun openDialog(dialog: EventDialogType){
        _uiState.update { it.copy(activeDialog = dialog) }
    }

    // endregion

    fun onFormAction(action: EventFormAction){
        when(action){
            is EventFormAction.ClearUiState -> clearUiState()
            is EventFormAction.LoadUiState -> loadUiState(action.event)
            is EventFormAction.SelectTemplate -> selectTemplate(action.template)
            is EventFormAction.UpdateEndTime -> updateEndTime()
            is EventFormAction.ValidateAndSave -> saveEvent(action.context)
        }
    }

    // region onFormAction function

    // Очистка состояния для создания
    private fun clearUiState() {
        _eventFormFields.value = EventFormFields()
        Log.d("DEBUG", "Clear State")
    }

    // Загрузка состояния события для редактирования
    private fun loadUiState(event: Event) {
        var uiState = _eventFormFields.value

        _selectedId.value = event.id
        _selectedTemplate.value = event.template
        uiState.name.edit { replace(0, length, event.name.toString()) }
        uiState.description.edit { replace(0, length, event.description.toString()) }
        uiState.startTime.edit { replace(0, length, getStringByTime(event.start_time)) }
        uiState.endTime.edit { replace(0, length, getStringByTime(event.end_time)) }
    }

    private fun selectTemplate(template: EventTemplate?){
        _selectedTemplate.value = template
        onTemplateSelect()
    }

    // Комплексный вызов функций изменения полей ввода при выборе шаблона
    private fun onTemplateSelect() {
        replaceTemplateName()
        updateEndTime()
    }

    // Заменяет имя события на шаблон вида "{Template.Name} - "
    private fun replaceTemplateName(){

        val form = _eventFormFields.value

        form.name.edit { replace(0, length ,
            if(form.template != null) "${form.template.name} - " else ""
        ) }
    }

    // Обновляет время конца события на основе длительности шаблона и времни начала
    private fun updateEndTime(){
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

    // Сохранение(изменение) события
    private fun saveEvent(context: Context) {
        val state = _eventFormFields.value

        viewModelScope.launch {
            if (isDataValid(context ,state)) {
                val event = Event(
                    id = state.id, // null для создания, ID для обновления
                    user = session.user.value,
                    template = state.template,
                    name = state.name.text.toString(),
                    description = if(state.description.text == "") null else state.description.text.toString(),
                    image = state.template?.image,
                    start_time = getTimeByString(state.startTime.text.toString()),
                    end_time = getTimeByString(state.endTime.text.toString())
                )
                if (state.id == null) {
                    eventRepository.addEvent(event)
                    _validationEvents.send(ValidationEvent.SuccessCreate(context))
                } else {
                    eventRepository.updateEvent(event)
                    _validationEvents.send(ValidationEvent.SuccessUpdate(context))
                }
            }
        }
    }

    // Валидация полей формы
    private suspend fun isDataValid(context: Context ,state: EventFormFields): Boolean {
        return when {
            state.name.text.isEmpty() -> {
                _validationEvents.send(ValidationEvent.EmptyName(context))
                false
            }

            _eventFormFields.value.startTime.text.isEmpty() || _eventFormFields.value.endTime.text.isEmpty() -> {
                _validationEvents.send(ValidationEvent.EmptyTime(context))
                false
            }

            !validateTime() -> {
                _validationEvents.send(ValidationEvent.InvalidTime(context))
                false
            }
            else -> true
        }
    }

    // Проверка времени на то что начало не позднее конца, и то что они заполнены
    private fun validateTime(): Boolean {
        val startTime = getTimeByString(_eventFormFields.value.startTime.text.toString())
        val endTime = getTimeByString(_eventFormFields.value.endTime.text.toString())

        return startTime.compareTo(endTime) < 0
    }

    // endregion

    // region form validation

    fun onValidation(event: ValidationEvent){
        when(event){
            is ValidationEvent.EmptyName -> {
                Toast.makeText(
                    event.context,
                    "Имя события должно быть заполнено",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is ValidationEvent.EmptyTime -> {
                Toast.makeText(
                    event.context,
                    "Время не заполнено",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is ValidationEvent.InvalidTime -> {
                Toast.makeText(
                    event.context,
                    "Время начала должно быть раньше чем время окончания",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is ValidationEvent.SuccessCreate -> {
                Toast.makeText(
                    event.context,
                    "Успешно создано!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is ValidationEvent.SuccessUpdate -> {
                Toast.makeText(
                    event.context,
                    "Успешно изменено!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // endregion

    // region utility function

    // Получение времени из строки по формату
    private fun getTimeByString(time: String, format: DateTimeFormatter = dateTimeFormat): LocalDateTime{
        return LocalDateTime.parse(time, format)
    }

    // Получение строки из времени по формату
    private fun getStringByTime(time: LocalDateTime, format: DateTimeFormatter=dateTimeFormat): String{
        return time.format(format)
    }

    // Добавление длительности к начальному времение, используется для end_time
    private fun addDuration(startTime: String, duration: Long): String {
        var startTimeInTime =
            getTimeByString(startTime)

        return startTimeInTime.plusSeconds(duration / 1000)
            .format(dateTimeFormat)
    }

    // endregion













}
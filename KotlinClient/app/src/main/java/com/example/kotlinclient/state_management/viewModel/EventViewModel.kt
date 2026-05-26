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
import kotlinx.coroutines.flow.combine
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
    data class LoadUiState(val event: Event, val onSuccess: () -> Unit): EventFormAction
    data class ValidateAndSave(val context: Context): EventFormAction
    data object UpdateEndTime: EventFormAction
}

// Запечатанный интерфейс событий валидации
sealed interface EventFormValidationEvent {
    data class EmptyName(val context: Context) : EventFormValidationEvent
    data class EmptyTime(val context: Context) : EventFormValidationEvent
    data class InvalidTime(val context: Context) : EventFormValidationEvent
    data class SuccessCreate(val context: Context) : EventFormValidationEvent
    data class SuccessUpdate(val context: Context): EventFormValidationEvent
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
    private val _validationEvents = Channel<EventFormValidationEvent>()
    val validationEvents = _validationEvents.receiveAsFlow()

    // Состояние всего экрана событий
    // Используется combine потому что зависит от другого потока
    // отслеживает изменение в базе + изменение через update
    private val activeDialog = MutableStateFlow<EventDialogType?>(null)

    val uiState = combine( eventRepository.getAllEventsWithTemplate(), activeDialog ){
        events, dialog ->   EventUiState(events, dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventUiState())

    // Состояние EventCreateModal
    // Используется MSF потому что нет потоков, на основе которых можно было бы создать поток
    // Изменение происходит через update
    private val _eventFormFields = MutableStateFlow(EventFormFields())
    val eventFormFields = _eventFormFields.asStateFlow()

    // endregion


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
        activeDialog.value = null
    }

    // Открытие основных диаголов
    private fun openDialog(dialog: EventDialogType){
        activeDialog.value = dialog
    }

    // endregion

    fun onFormAction(action: EventFormAction){
        when(action){
            is EventFormAction.ClearUiState -> clearUiState()
            is EventFormAction.LoadUiState -> loadUiState(action.event, action.onSuccess)
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
    private fun loadUiState(event: Event, onSuccess: () -> Unit) {
        var uiState = _eventFormFields.value

        _eventFormFields.update { it.copy(id=event.id, template=event.template) }
        uiState.name.edit { replace(0, length, event.name.toString()) }
        uiState.description.edit { replace(0, length, event.description.toString()) }
        uiState.startTime.edit { replace(0, length, getStringByTime(event.start_time)) }
        uiState.endTime.edit { replace(0, length, getStringByTime(event.end_time)) }

        onSuccess()
    }

    private fun selectTemplate(template: EventTemplate?){
        _eventFormFields.update { it.copy(template= template) }
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
                    _validationEvents.send(EventFormValidationEvent.SuccessCreate(context))
                } else {
                    eventRepository.updateEvent(event)
                    _validationEvents.send(EventFormValidationEvent.SuccessUpdate(context))
                }
            }
        }
    }

    // Валидация полей формы
    private suspend fun isDataValid(context: Context ,state: EventFormFields): Boolean {
        return when {
            state.name.text.isEmpty() -> {
                _validationEvents.send(EventFormValidationEvent.EmptyName(context))
                false
            }

            _eventFormFields.value.startTime.text.isEmpty() || _eventFormFields.value.endTime.text.isEmpty() -> {
                _validationEvents.send(EventFormValidationEvent.EmptyTime(context))
                false
            }

            !validateTime() -> {
                _validationEvents.send(EventFormValidationEvent.InvalidTime(context))
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

    fun onValidation(event: EventFormValidationEvent){
        when(event){
            is EventFormValidationEvent.EmptyName -> {
                Toast.makeText(
                    event.context,
                    "Имя события должно быть заполнено",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is EventFormValidationEvent.EmptyTime -> {
                Toast.makeText(
                    event.context,
                    "Время не заполнено",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is EventFormValidationEvent.InvalidTime -> {
                Toast.makeText(
                    event.context,
                    "Время начала должно быть раньше чем время окончания",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is EventFormValidationEvent.SuccessCreate -> {
                Toast.makeText(
                    event.context,
                    "Успешно создано!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is EventFormValidationEvent.SuccessUpdate -> {
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
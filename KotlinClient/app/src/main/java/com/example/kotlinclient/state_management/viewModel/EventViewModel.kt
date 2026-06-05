package com.example.kotlinclient.state_management.viewModel


import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.utility.EventAlarmScheduler
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
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
sealed interface EventDialogType {
    data object Create : EventDialogType
    data object Edit : EventDialogType
    data class View(val eventData: Event) : EventDialogType
}

// Запечатанный интерфейс действий на экране событий
sealed interface EventAction {
    data class DeleteEvent(val id: Long) : EventAction
    data object DismissDialog : EventAction
    data class OpenDialog(val dialog: EventDialogType) : EventAction

    data class OnFormAction(val action: EventFormAction) : EventAction

}

// endregion

// region EventForm

// дата класс состояния формы создания
data class EventFormUiState(
    val id: Long? = null,
    val name: TextFieldState = TextFieldState(""),
    val description: TextFieldState = TextFieldState(""),
    val startTime: TextFieldState = TextFieldState(""),
    val endTime: TextFieldState = TextFieldState(""),
    val template: EventTemplate? = null,
    val errors: EventFormErrors = EventFormErrors()
)

// Запечатанный интерфейс действий формы создания
sealed interface EventFormAction {
    data object ClearUiState : EventFormAction
    data class SelectTemplate(val template: EventTemplate?) : EventFormAction
    data class LoadUiState(val event: Event) : EventFormAction
    data object ValidateAndSave : EventFormAction
    data object UpdateEndTime : EventFormAction
    data class OnFormValidation(val action: EventFormValidation) : EventFormAction
}

data class EventFormErrors(
    val nameError: String? = null,
    val startTimeError: String? = null,
    val endTimeError: String? = null,
)

sealed interface EventFormValidation {
    data object ValidateName : EventFormValidation
    data object ClearNameError : EventFormValidation
    data object ValidateStartTime : EventFormValidation
    data object ClearStartTimeError : EventFormValidation
    data object ValidateEndTime : EventFormValidation
    data object ClearEndTimeError : EventFormValidation
}

// Запечатанный интерфейс событий валидации
sealed interface EventFormNotificationEvent {
    data object SuccessCreate : EventFormNotificationEvent
    data object SuccessUpdate : EventFormNotificationEvent
}

// endregion

class EventViewModel(
    val eventRepository: EventRepository,
    val session: UserSessionProvider,
    val alarmScheduler: EventAlarmScheduler,
) : ViewModel() {

    // Формат представления даты и времени
    val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")

    // region flows

    // Канал валидации формы создания(изменения) событий
    private val _notificationEvent = Channel<EventFormNotificationEvent>()
    val notificationEvent = _notificationEvent.receiveAsFlow()

    // Состояние всего экрана событий
    // Используется combine потому что зависит от другого потока
    // отслеживает изменение в базе + изменение через update
    private val activeDialog = MutableStateFlow<EventDialogType?>(null)

    val uiState =
        combine(eventRepository.getAllEventsWithTemplate(), activeDialog) { events, dialog ->
            EventUiState(events, dialog)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventUiState())

    // Состояние EventCreateModal
    // Используется MSF потому что нет потоков, на основе которых можно было бы создать поток
    // Изменение происходит через update
    private val _formUiState = MutableStateFlow(EventFormUiState())
    val formUiState = _formUiState.asStateFlow()

    // endregion


    // Поток состояния текущего времени обновляющийся раз в минуту
    val currentTime = flow {
        while (true) {
            emit(LocalDateTime.now())
            Log.d("DEBUG", "current Time ${LocalDateTime.now(ZoneId.systemDefault())}")
            delay(60_000) // Обновляем раз в минуту
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalDateTime.now())


    fun onAction(action: EventAction) {
        when (action) {
            is EventAction.DeleteEvent -> deleteEvent(action.id)
            is EventAction.DismissDialog -> dismissDialog()
            is EventAction.OpenDialog -> openDialog(action.dialog)
            is EventAction.OnFormAction -> onFormAction(action.action)
        }
    }

    // region onAction function

    // Удаление события
    private fun deleteEvent(id: Long) {

        viewModelScope.launch {
            eventRepository.deleteEventById(id)
            alarmScheduler.cancel(id)
        }
    }

    // Закрытие основных диалогов
    private fun dismissDialog() {
        activeDialog.value = null
    }

    // Открытие основных диаголов
    private fun openDialog(dialog: EventDialogType) {
        activeDialog.value = dialog
    }

    // endregion

    fun onFormAction(action: EventFormAction) {
        when (action) {
            is EventFormAction.ClearUiState -> clearUiState()
            is EventFormAction.LoadUiState -> loadUiState(action.event)
            is EventFormAction.SelectTemplate -> selectTemplate(action.template)
            is EventFormAction.UpdateEndTime -> updateEndTime()
            is EventFormAction.ValidateAndSave -> saveEvent()
            is EventFormAction.OnFormValidation -> onFormValidation(action.action)
        }
    }

    // region onFormAction function

    // Очистка состояния для создания
    private fun clearUiState() {
        _formUiState.value = EventFormUiState()
        Log.d("DEBUG", "Clear State")
    }

    // Загрузка состояния события для редактирования
    private fun loadUiState(event: Event) {
        val uiState = _formUiState.value

        _formUiState.update {
            it.copy(
                id = event.id,
                template = event.template,
                errors = EventFormErrors()
            )
        }
        uiState.name.edit { replace(0, length, event.name.toString()) }
        uiState.description.edit { replace(0, length, event.description.toString()) }
        uiState.startTime.edit { replace(0, length, getStringByTime(event.startTime)) }
        uiState.endTime.edit { replace(0, length, getStringByTime(event.endTime)) }
    }

    private fun selectTemplate(template: EventTemplate?) {
        _formUiState.update { it.copy(template = template) }
        onTemplateSelect()
        if (!_formUiState.value.startTime.text.isBlank()) {
            validateStartTime()
            validateEndTime()
        }
    }

    // Комплексный вызов функций изменения полей ввода при выборе шаблона
    private fun onTemplateSelect() {
        replaceTemplateName()
        updateEndTime()
    }

    // Заменяет имя события на шаблон вида "{Template.Name} - "
    private fun replaceTemplateName() {

        val form = _formUiState.value

        form.name.edit {
            replace(
                0, length,
                if (form.template != null) "${form.template.name} - " else ""
            )
        }
    }

    // Обновляет время конца события на основе длительности шаблона и времни начала
    private fun updateEndTime() {
        if (!_formUiState.value.startTime.text.isEmpty()) {
            _formUiState.value.endTime.edit {
                replace(
                    0,
                    length,
                    addDuration(
                        _formUiState.value.startTime.text.toString(),
                        _formUiState.value.template?.duration ?: 0
                    )
                )
            }
        }
    }

    // Сохранение(изменение) события
    private fun saveEvent() {
        val state = _formUiState.value

        viewModelScope.launch {
            if (isDataValid()) {
                val event = Event(
                    id = state.id, // null для создания, ID для обновления
                    user = session.user.value,
                    template = state.template,
                    name = state.name.text.toString().trim(),
                    description = if (state.description.text == "") null else state.description.text.toString().trim(),
                    image = state.template?.image,
                    startTime = getTimeByString(state.startTime.text.toString()),
                    endTime = getTimeByString(state.endTime.text.toString())
                )
                if (state.id == null) {
                    val savedEventId = eventRepository.addEvent(event)
                    _notificationEvent.send(EventFormNotificationEvent.SuccessCreate)
                    alarmScheduler.scheduleFinish(event.copy(id = savedEventId))
                } else {
                    alarmScheduler.cancel(event.id!!)
                    eventRepository.updateEvent(event)
                    onAction(EventAction.DismissDialog)
                    _notificationEvent.send(EventFormNotificationEvent.SuccessUpdate)
                    alarmScheduler.scheduleFinish(event)
                }
            }
        }
    }

    // Валидация полей формы
    private suspend fun isDataValid(): Boolean {
        return when {
            !validateName() -> false
            !validateStartTime() -> false
            !validateEndTime() -> false
            else -> true
        }
    }

    // endregion


    fun onFormValidation(action: EventFormValidation) {
        when (action) {
            EventFormValidation.ClearEndTimeError -> clearEndTimeError()
            EventFormValidation.ClearNameError -> clearNameError()
            EventFormValidation.ClearStartTimeError -> clearStartTimeError()
            EventFormValidation.ValidateEndTime -> validateEndTime()
            EventFormValidation.ValidateName -> validateName()
            EventFormValidation.ValidateStartTime -> validateStartTime()
        }
    }

    // region onFormValidation
    private fun validateName(): Boolean {
        val text = _formUiState.value.name.text
        val error = when {
            text.isBlank() -> "Имя события должно быть заполнено"
            text.length > 100 -> "Имя события слишком длинное"
            else -> null
        }

        _formUiState.update { it.copy(errors = it.errors.copy(nameError = error)) }

        return error == null
    }

    private fun clearNameError() {
        if (_formUiState.value.errors.nameError != null) {
            _formUiState.update { it.copy(errors = it.errors.copy(nameError = null)) }
        }
    }

    private fun validateStartTime(): Boolean {
        val text = _formUiState.value.startTime.text
        val error = when {
            text.isBlank() -> "Время начала дожно быть заполнено"
            _formUiState.value.template != null && _formUiState.value.endTime.text.isBlank() -> null
            _formUiState.value.template != null && !endTimeCompareCurrentTime() -> "Событие начинается слишком рано"
            else -> null
        }

        _formUiState.update { it.copy(errors = it.errors.copy(startTimeError = error)) }

        return error == null
    }

    private fun clearStartTimeError() {
        if (_formUiState.value.errors.startTimeError != null) {
            _formUiState.update { it.copy(errors = it.errors.copy(startTimeError = null)) }
        }
    }

    private fun validateEndTime(): Boolean {
        val text = _formUiState.value.endTime.text
        val error = when {
            text.isBlank() -> "Время окончания дожно быть заполнено"
            _formUiState.value.template == null && _formUiState.value.startTime.text.isBlank() -> null
            _formUiState.value.template == null && !startTimeCompareEndTime() -> "Событие не может закончиться раньше чем начнётся"
            _formUiState.value.template == null && !endTimeCompareCurrentTime() -> "Событие не может закончится в прошлом"
            else -> null
        }

        _formUiState.update { it.copy(errors = it.errors.copy(endTimeError = error)) }

        return error == null
    }

    private fun clearEndTimeError() {
        if (_formUiState.value.errors.nameError != null) {
            _formUiState.update { it.copy(errors = it.errors.copy(endTimeError = null)) }
        }
    }

    // endregion

    // region utility function

    // Получение времени из строки по формату
    fun getTimeByString(
        time: String,
        format: DateTimeFormatter = dateTimeFormat
    ): LocalDateTime {
        return LocalDateTime.parse(time, format)
    }

    // Получение строки из времени по формату
    fun getStringByTime(
        time: LocalDateTime,
        format: DateTimeFormatter = dateTimeFormat
    ): String {
        return time.format(format)
    }

    // Добавление длительности к начальному времение, используется для end_time
    private fun addDuration(startTime: String, duration: Long): String {
        val startTimeInTime =
            getTimeByString(startTime)

        return startTimeInTime.plusSeconds(duration / 1000)
            .format(dateTimeFormat)
    }

    private fun startTimeCompareEndTime(): Boolean {
        val startTime = getTimeByString(_formUiState.value.startTime.text.toString())
        val endTime = getTimeByString(_formUiState.value.endTime.text.toString())

        return startTime < endTime
    }

    private fun endTimeCompareCurrentTime(): Boolean {
        val endTime =
            getTimeByString(_formUiState.value.endTime.text.toString(), dateTimeFormat).atZone(
                ZoneId.systemDefault()
            ).toInstant()
        val currentTime = System.currentTimeMillis()

        return endTime.toEpochMilli() > currentTime

    }

    // endregion


}
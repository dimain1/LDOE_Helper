package com.example.kotlinclient.state_management.viewModel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.EventTemplateRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

// region EventTemplate screen

data class EventTemplateUiState(
    val templates: List<EventTemplate> = emptyList(),
    val activeDialog: EventTemplateDialogType? = null
)

sealed interface EventTemplateDialogType {
    data object Create : EventTemplateDialogType
    data object Edit : EventTemplateDialogType
    data class View(val initialData: EventTemplate?) : EventTemplateDialogType
}

sealed interface EventTemplateAction {
    data class DeleteTemplate(val id: Long) : EventTemplateAction
    data class OpenDialog(val dialog: EventTemplateDialogType) : EventTemplateAction
    data object DismissDialog : EventTemplateAction

    data class OnFormAction(val action: EventTemplateFormAction) : EventTemplateAction
}

data class EventTemplateFormUiState(
    val id: Long? = null,
    val image: String? = null,
    val name: TextFieldState = TextFieldState(""),
    val description: TextFieldState = TextFieldState(""),
    val duration: TextFieldState = TextFieldState(""),
    val errors: EventTemplateFormErrors = EventTemplateFormErrors()
)


sealed interface EventTemplateFormAction {
    data class CreateEventTemplate(val context: Context) : EventTemplateFormAction
    data class SaveImageInLocal(val context: Context, val uri: Uri?) : EventTemplateFormAction
    data object ClearUiState : EventTemplateFormAction
    data class LoadUiState(val template: EventTemplate) : EventTemplateFormAction

    data class OnFormValidation(val action: EventTemplateFormValidation) : EventTemplateFormAction
}

data class EventTemplateFormErrors(
    val nameError: String? = null,
    val durationError: String? = null
)

sealed interface EventTemplateFormValidation {
    data object ValidateName : EventTemplateFormValidation
    data object ClearNameError : EventTemplateFormValidation
    data object ValidateDuration : EventTemplateFormValidation
    data object ClearDurationError : EventTemplateFormValidation
}

sealed interface EventTemplateFormNotificationEvent {
    data class ImageLoadError(val context: Context) : EventTemplateFormNotificationEvent
    data class SuccessCreate(val context: Context) : EventTemplateFormNotificationEvent
    data class SuccesssUpdate(val context: Context) : EventTemplateFormNotificationEvent
}

// endregion

class EventTemplateViewModel(
    val eventTemplateRepository: EventTemplateRepository,
    val session: UserSessionProvider
) : ViewModel() {

    // region flows

    private val activeDialog = MutableStateFlow<EventTemplateDialogType?>(null)

    val uiState = combine(
        eventTemplateRepository.getAllTemplateWithUser(),
        activeDialog
    ) { templates, dialog ->
        EventTemplateUiState(templates, dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventTemplateUiState())


    private val _formUiState = MutableStateFlow(EventTemplateFormUiState())

    val formUiState = _formUiState.asStateFlow()


    private val _notificationEvent = Channel<EventTemplateFormNotificationEvent>()

    val notificationEvent = _notificationEvent.receiveAsFlow()


    // endregion

    fun onAction(action: EventTemplateAction) {
        when (action) {
            is EventTemplateAction.DeleteTemplate -> deleteTemplate(action.id)
            is EventTemplateAction.OpenDialog -> openDialog(action.dialog)
            is EventTemplateAction.DismissDialog -> dismissDialog()
            is EventTemplateAction.OnFormAction -> onFormAction(action.action)
        }
    }

    // region onAction function

    private fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            eventTemplateRepository.deleteTemplateById(id)
        }
    }

    private fun openDialog(dialog: EventTemplateDialogType) {
        activeDialog.value = dialog
    }

    private fun dismissDialog() {
        activeDialog.value = null
    }

    //endregion

    fun onFormAction(action: EventTemplateFormAction) {
        when (action) {
            is EventTemplateFormAction.CreateEventTemplate -> saveTemplate(action.context)
            is EventTemplateFormAction.SaveImageInLocal -> saveImageInLocal(
                action.context,
                action.uri
            )

            is EventTemplateFormAction.ClearUiState -> clearFormUiState()
            is EventTemplateFormAction.LoadUiState -> loadFormUiState(action.template)
            is EventTemplateFormAction.OnFormValidation -> onFormValidation(action.action)
        }
    }

    // region onFormAction function

    private suspend fun isDataValid(): Boolean {
        if (!validateName()) {
            return false
        }
        if (!validateDuration()) {
            return false
        }
        return true
    }

    private fun saveTemplate(context: Context) {
        val state = _formUiState.value

        viewModelScope.launch {

            if (isDataValid()) {

                val template = EventTemplate(
                    id = state.id,
                    creator = session.user.value,
                    name = state.name.text.toString(),
                    description = state.description.text.toString(),
                    image = state.image,
                    duration = state.duration.text.toString().toLong()
                )
                if (state.id == null) {
                    eventTemplateRepository.createTemplate(template)
                    _notificationEvent.send(EventTemplateFormNotificationEvent.SuccessCreate(context))
                } else {
                    eventTemplateRepository.updateTemplate(template)
                    onAction(EventTemplateAction.DismissDialog)
                    _notificationEvent.send(
                        EventTemplateFormNotificationEvent.SuccesssUpdate(
                            context
                        )
                    )
                }

            }


        }
    }

    private fun saveImageInLocal(context: Context, uri: Uri?) {
        try {
            val imagesDir = File(context.filesDir, "template_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val fileName = "${UUID.randomUUID()}.jpg"
            val destinationFile = File(imagesDir, fileName)

            if (uri == null) {
                _formUiState.update { it.copy(image = null) }
                return
            }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            _formUiState.update { it.copy(image = destinationFile.absolutePath) }
        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch {
                _notificationEvent.send(EventTemplateFormNotificationEvent.ImageLoadError(context))
            }
            null
        }
    }

    private fun clearFormUiState() {
        _formUiState.value = EventTemplateFormUiState()
    }

    private fun loadFormUiState(template: EventTemplate) {
        _formUiState.update { it.copy(id = template.id, image = template.image, errors = EventTemplateFormErrors()) }
        _formUiState.value.name.edit { replace(0, length, template.name) }
        _formUiState.value.description.edit { replace(0, length, template.description ?: "") }
        _formUiState.value.duration.edit { replace(0, length, template.duration.toString()) }
    }

    // endregion

    fun onFormValidation(action: EventTemplateFormValidation) {
        when (action) {
            is EventTemplateFormValidation.ValidateName -> validateName()
            is EventTemplateFormValidation.ClearNameError -> clearNameError()
            is EventTemplateFormValidation.ValidateDuration -> validateDuration()
            is EventTemplateFormValidation.ClearDurationError -> clearDurationError()
        }
    }

    // region onFormValidationFunction

    private fun validateName(): Boolean {
        val text = _formUiState.value.name.text
        val error = when {
            text.isBlank() -> "Имя шаблона не заполнено"
            text.toString().length > 60 -> "Имя шаблона слишком длинное"
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

    private fun validateDuration(): Boolean {
        val text = _formUiState.value.duration.text
        val error = when {
            text.isBlank() -> "Длительность не указана"
            text.toString().toLongOrNull() == null -> "Некорректный формат длительности"
            else -> null
        }

        _formUiState.update { it.copy(errors = it.errors.copy(durationError = error)) }

        return error == null
    }

    private fun clearDurationError() {
        if (_formUiState.value.errors.durationError != null) {
            _formUiState.update { it.copy(errors = it.errors.copy(durationError = null)) }
        }
    }

    // endregion

    // region NotificationUser

    fun onNotification(action: EventTemplateFormNotificationEvent) {
        when (action) {
            is EventTemplateFormNotificationEvent.ImageLoadError -> Toast.makeText(
                action.context,
                "Во время выбора изображения произошла ошибка, попробуйте снова",
                Toast.LENGTH_SHORT
            ).show()

            is EventTemplateFormNotificationEvent.SuccessCreate -> Toast.makeText(
                action.context,
                "Шаблон успешно создан",
                Toast.LENGTH_SHORT
            ).show()

            is EventTemplateFormNotificationEvent.SuccesssUpdate -> Toast.makeText(
                action.context,
                "Шаблон успешно изменён",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    // endregion


}
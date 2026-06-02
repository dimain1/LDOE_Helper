package com.example.kotlinclient.presentation.event.modal


import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.presentation.utility.uiComponent.BasicTextFieldInModal
import com.example.kotlinclient.presentation.utility.modal.DateTimePicker
import com.example.kotlinclient.presentation.utility.getStringTimeByDuration
import com.example.kotlinclient.presentation.utility.uiComponent.ValidatedBasicTextFieldInModal
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.viewModel.EventFormAction
import com.example.kotlinclient.state_management.viewModel.EventFormUiState
import com.example.kotlinclient.state_management.viewModel.EventFormValidation
import com.example.kotlinclient.ui.theme.Typography
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun EventCreateModal(
    onDismiss: () -> Unit,
    templates: List<EventTemplate>,
    eventFormFields: EventFormUiState,
    onFormAction: (EventFormAction) -> Unit,
) {

    val context: Context = LocalContext.current
    val currentFields by rememberUpdatedState(eventFormFields)

    LaunchedEffect(Unit) {
        snapshotFlow {
            currentFields.startTime.text
        }
            .onEach { text ->
                if (currentFields.template != null) {
                    onFormAction(EventFormAction.UpdateEndTime)
                    onFormAction(EventFormAction.OnFormValidation(EventFormValidation.ValidateStartTime))
                }
            }
            .collect()
    }

    // region EventTemplatePicker

    var showTemplateModal by remember { mutableStateOf(false) }

    EventTemplatePicker(
        showTemplateModal = showTemplateModal,
        templates = templates,
        selectedTemplate = eventFormFields.template,
        onDismiss = { showTemplateModal = false },
        onClick = { template ->
            onFormAction(
                EventFormAction.SelectTemplate(template)
            )
            Log.d("DEBUG", "SELECTED ID IN MAIN MODAL ${eventFormFields.template?.id ?: "выфвфв"}")
        }

    )

    // endregion

    // region DateTimePicker

    var showDateTimePicker by remember { mutableStateOf(false) }

    var activeTextField by remember { mutableStateOf<TextFieldState?>(null) }

    DateTimePicker(
        showDateTimePicker,
        initialDateTime = if (activeTextField?.text?.isEmpty() ?: false) LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")) else activeTextField?.text.toString(),
        onDismiss =
            {
                showDateTimePicker = false
                activeTextField = null
            },
        onConfirm = {
            text -> activeTextField?.edit { replace(0, length, text) }
            when(activeTextField){
                eventFormFields.startTime -> onFormAction(EventFormAction.OnFormValidation(
                    EventFormValidation.ValidateStartTime))
                eventFormFields.endTime -> onFormAction(EventFormAction.OnFormValidation(
                    EventFormValidation.ValidateEndTime))
            }

        }
    )

    // endregion

    val focusManager = LocalFocusManager.current
    val modalFocusRequester = remember { FocusRequester() }

    val clearFocusModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            modalFocusRequester.requestFocus()
            focusManager.clearFocus()
        })
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(modalFocusRequester)
                    .focusable()
                    .then(clearFocusModifier), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (eventFormFields.id == null) "Создание события" else "Редактирование события",
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .focusRequester(modalFocusRequester)
                    .focusable()
                    .then(clearFocusModifier)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(10)
                        )
                        .border(2.dp, colorScheme.outline, RoundedCornerShape(10))
                        .padding(12.dp)

                ) {
                    if (eventFormFields.template == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {}
                        Image(
                            painter = painterResource(R.drawable.pencil),
                            contentDescription = "Выбрать шаблон",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(onClick = {
                                    modalFocusRequester.requestFocus()
                                    focusManager.clearFocus()
                                    showTemplateModal = true
                                }),
                            colorFilter = ColorFilter.tint(colorScheme.primary)
                        )
                    } else {
                        // Изображение события !!!!!!!!!!!!!!!!
                        LocalImage(
                            eventFormFields.template?.image,
                            Modifier.size(48.dp)
                        )

                        Spacer(Modifier.width(12.dp))
                        // Содержание события
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        {
                            // Название события
                            Text(
                                text = eventFormFields.template?.name ?: "",
                                style = TextStyle(
                                    fontSize = Typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            // Длительность
                            Text(
                                text = if (eventFormFields.template.duration < 60_000L) "Меньше минуты"
                                else if (eventFormFields.template.duration > 86_400_000L * 30) "Больше месяца"
                                else {
                                    getStringTimeByDuration(eventFormFields.template.duration)
                                },
                                style = TextStyle(
                                    fontSize = Typography.bodySmall.fontSize,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = colorScheme.secondary,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )

                        }
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Убрать шаблон",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(onClick = {
                                    modalFocusRequester.requestFocus()
                                    focusManager.clearFocus()
                                    onFormAction(EventFormAction.SelectTemplate(template = null))
                                }),
                            tint = colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                ValidatedBasicTextFieldInModal(
                    state = eventFormFields.name,
                    placeholder = "Название",
                    onFocusLost = {
                        onFormAction(
                            EventFormAction.OnFormValidation(
                                EventFormValidation.ValidateName
                            )
                        )
                    },
                    onFocused = { onFormAction(EventFormAction.OnFormValidation(EventFormValidation.ClearNameError)) },
                    errorText = eventFormFields.errors.nameError
                )
                Spacer(Modifier.height(16.dp))
                BasicTextFieldInModal(eventFormFields.description, "Описание")
                Spacer(Modifier.height(16.dp))

                ValidatedBasicTextFieldInModal(
                    state = eventFormFields.startTime,
                    placeholder = "Время начала",
                    onClick = {
                        modalFocusRequester.requestFocus()
                        focusManager.clearFocus()

                        onFormAction(EventFormAction.OnFormValidation(EventFormValidation.ClearStartTimeError))

                        activeTextField = eventFormFields.startTime
                        showDateTimePicker = true
                    },
                    readOnly = true,
                    onFocusLost = {},
                    onFocused = {},
                    errorText = eventFormFields.errors.startTimeError
                )
                Spacer(Modifier.height(16.dp))

                ValidatedBasicTextFieldInModal(
                    state = eventFormFields.endTime,
                    placeholder = "Время окончания",
                    onClick = {
                        if (eventFormFields.template == null)
                        {
                            modalFocusRequester.requestFocus()
                            focusManager.clearFocus()

                            onFormAction(EventFormAction.OnFormValidation(EventFormValidation.ClearEndTimeError))

                            activeTextField = eventFormFields.endTime
                            showDateTimePicker = true
                        }
                        else { }
                    },
                    readOnly = true,
                    onFocusLost = {},
                    onFocused = {},
                    errorText = eventFormFields.errors.endTimeError
                )

            }
        },
        confirmButton =
            {
                Button(
                    onClick = {
                        onFormAction(EventFormAction.ValidateAndSave)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.tertiary,
                        contentColor = Color.White
                    )
                )
                {
                    Text(
                        text = if (eventFormFields.id == null) "Создать" else "Сохранить",
                        style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            },
        dismissButton =
            {
                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.tertiary,
                        contentColor = Color.White
                    )
                )
                {
                    Text(
                        text = "Отменить",
                        style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            },
        containerColor = colorScheme.secondaryContainer
    )

}


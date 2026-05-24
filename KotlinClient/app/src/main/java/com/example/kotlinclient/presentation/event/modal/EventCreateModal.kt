package com.example.kotlinclient.presentation.event.modal


import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.LocalImage
import com.example.kotlinclient.presentation.utility.DateTimePicker
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.viewModel.EventFormAction
import com.example.kotlinclient.state_management.viewModel.EventFormFields
import com.example.kotlinclient.ui.theme.Typography
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun EventCreateModal(
    onDismiss: () -> Unit,
    templates: List<EventTemplate>,
    eventFormFields: EventFormFields,
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
                }
            }
            .collect()
    }

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
        onConfirm = { text -> activeTextField?.edit { replace(0, length, text) } }
    )

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = if (eventFormFields.id == null) "Создать событие" else "Редактировать событие")
        },
        text = {
            Column() {

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
                            contentDescription = "Select template",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(onClick = {
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
                                text = (eventFormFields.template?.duration!! / 1000 / 60).toString() + " Minutes",
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
                            contentDescription = "Select Template",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(onClick = {
                                    onFormAction(EventFormAction.SelectTemplate(template = null))
                                }),
                            tint = colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                BasicTextFieldInModal(eventFormFields.name, "Название")
                Spacer(Modifier.height(16.dp))
                BasicTextFieldInModal(eventFormFields.description, "Описание")
                Spacer(Modifier.height(16.dp))
                BasicTextFieldInModal(
                    eventFormFields.startTime, "Время начала",
                    onClick = {
                        activeTextField = eventFormFields.startTime
                        showDateTimePicker = true
                    },
                    readOnly = true
                )
                Spacer(Modifier.height(16.dp))
                BasicTextFieldInModal(
                    eventFormFields.endTime, "Время окончания", true,
                    onClick = if (eventFormFields.template == null) {
                        {
                            activeTextField = eventFormFields.endTime
                            showDateTimePicker = true
                        }
                    } else {
                        {}
                    }
                )

            }
        },
        confirmButton =
            {
                Button(
                    onClick = {
                        onFormAction(EventFormAction.ValidateAndSave(context))
                        if (eventFormFields.id != null) onDismiss()
                    },

                    )
                {
                    Text(if (eventFormFields.id == null) "Создать" else "Сохранить")
                }
            },
        dismissButton =
            {
                Button(
                    onClick = { onDismiss() }
                )
                {
                    Text("Отменить")
                }
            },
        containerColor = colorScheme.secondaryContainer
    )

}

@Composable
fun BasicTextFieldInModal(
    state: TextFieldState,
    placeholder: String,
    readOnly: Boolean = false,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    val isPressed by interactionSource.collectIsPressedAsState()
    if (isPressed) {
        LaunchedEffect(Unit) {
            onClick()
        }
    }

    BasicTextField(
        state = state,
        enabled = true,
        readOnly = readOnly,
        interactionSource = interactionSource,
        modifier = Modifier
            .heightIn(48.dp, 96.dp)
            .fillMaxWidth()
            .border(2.dp, colorScheme.outline, shape = RoundedCornerShape(10)),
        decorator = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 4.dp)
            ) {
                if (state.text.isEmpty()) {
                    Text(
                        "${placeholder}" +
                                "", style = Typography.bodyLarge, color = colorScheme.secondary
                    )
                }
                innerTextField()
            }
        },
        cursorBrush = SolidColor(colorScheme.primary),
        textStyle = Typography.bodyLarge.copy(color = colorScheme.primary)
    )
}
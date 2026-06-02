package com.example.kotlinclient.presentation.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.utility.uiComponent.BasicTextFieldInModal
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.presentation.utility.uiComponent.ValidatedBasicTextFieldInModal
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.ui.theme.Typography
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun OverlayCreateScreen(
    controller: OverlayController,
    onBack: () -> Unit
) {
    // ── State ─────────────────────────────────────────────────────────────────
    val templates     = remember { mutableStateListOf<EventTemplate>() }
    val name          = remember { TextFieldState("") }
    val description   = remember { TextFieldState("") }
    val startTime     = remember { TextFieldState("") }
    val endTime       = remember { TextFieldState("") }

    var selectedTemplate by remember { mutableStateOf<EventTemplate?>(null) }
    var nameError        by remember { mutableStateOf<String?>(null) }
    var startTimeError   by remember { mutableStateOf<String?>(null) }
    var endTimeError     by remember { mutableStateOf<String?>(null) }
    var activeModal      by remember { mutableStateOf<OverlayCreateModal?>(null) }

    val dateTimeFormat = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm") }
    var startDateText  by remember { mutableStateOf("") }
    var startTimeText  by remember { mutableStateOf("") }
    var endDateText    by remember { mutableStateOf("") }
    var endTimeText    by remember { mutableStateOf("") }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun updateField(field: TextFieldState, date: String, time: String) {
        if (date.isNotBlank() && time.isNotBlank())
            field.edit { replace(0, length, "$date - $time") }
    }

    fun parseDateTime(value: String): LocalDateTime? =
        runCatching { LocalDateTime.parse(value, dateTimeFormat) }.getOrNull()

    fun updateEndFromTemplate(template: EventTemplate? = selectedTemplate) {
        template ?: return
        val start = parseDateTime(startTime.text.toString()) ?: return
        endTime.edit {
            replace(0, length, start.plusSeconds(template.duration / 1000).format(dateTimeFormat))
        }
    }

    fun validate(): Boolean {
        val parsedStart = parseDateTime(startTime.text.toString())
        val parsedEnd   = parseDateTime(endTime.text.toString())

        nameError = when {
            name.text.isBlank()    -> "Название обязательно"
            name.text.length > 100 -> "Название слишком длинное"
            else                   -> null
        }
        startTimeError = when {
            startTime.text.isBlank() -> "Укажите время начала"
            parsedStart == null      -> "Неверный формат времени"
            else                     -> null
        }
        endTimeError = when {
            endTime.text.isBlank()                            -> "Укажите время окончания"
            parsedEnd == null                                 -> "Неверный формат времени"
            parsedStart != null && parsedEnd <= parsedStart  -> "Конец должен быть позже начала"
            parsedEnd <= LocalDateTime.now()                  -> "Время уже прошло"
            else                                              -> null
        }
        return nameError == null && startTimeError == null && endTimeError == null
    }

    LaunchedEffect(Unit) {
        controller.observeTemplates { templates.clear(); templates.addAll(it) }
    }

    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp

    // ── Layout ────────────────────────────────────────────────────────────────
    // Box нужен, чтобы модалки рисовались поверх формы
    Box(contentAlignment = Alignment.Center) {

        // ── Form ─────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .background(colorScheme.secondaryContainer, RoundedCornerShape(14.dp))
                .border(1.dp, colorScheme.outline, RoundedCornerShape(14.dp))
                .heightIn(max = screenHeightDp * 0.88f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Создать событие",
                    style = TextStyle(fontSize = Typography.titleMedium.fontSize, fontWeight = FontWeight.Bold),
                    color = colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                OverlayButton(text = "Назад", onClick = onBack)
            }

            Spacer(Modifier.height(14.dp))

            // ── Template card ─────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeModal = OverlayCreateModal.Template },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                border = BorderStroke(
                    width = if (selectedTemplate != null) 2.dp else 1.dp,
                    color = if (selectedTemplate != null) colorScheme.tertiary else colorScheme.outline
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LocalImage(selectedTemplate?.image, Modifier.size(46.dp))

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedTemplate?.name ?: "Выбрать шаблон",
                            style = TextStyle(
                                fontSize = Typography.bodyMedium.fontSize,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colorScheme.primary,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2
                        )
                        Text(
                            text = if (selectedTemplate != null) "Шаблон выбран" else "Необязательно",
                            style = Typography.bodyMedium,
                            color = colorScheme.secondary
                        )
                    }

                    if (selectedTemplate != null) {
                        Image(
                            painter = painterResource(R.drawable.pencil),
                            contentDescription = "Изменить шаблон",
                            modifier = Modifier.size(22.dp),
                            colorFilter = ColorFilter.tint(colorScheme.secondary)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            ValidatedBasicTextFieldInModal(
                state = name,
                placeholder = "Название",
                onFocusLost = { validate() },
                onFocused = { nameError = null },
                errorText = nameError
            )

            Spacer(Modifier.height(12.dp))

            BasicTextFieldInModal(description, "Описание")

            Spacer(Modifier.height(12.dp))

            ValidatedBasicTextFieldInModal(
                state = startTime,
                placeholder = "Время начала",
                onClick = { activeModal = OverlayCreateModal.StartDateTime; startTimeError = null },
                readOnly = true,
                onFocusLost = {
                    startTimeError = null
                    if (selectedTemplate != null) updateEndFromTemplate()
                },
                onFocused = { startTimeError = null },
                errorText = startTimeError
            )

            Spacer(Modifier.height(12.dp))

            ValidatedBasicTextFieldInModal(
                state = endTime,
                placeholder = "Время окончания",
                onClick = {
                    if (selectedTemplate == null) {
                        activeModal = OverlayCreateModal.EndDateTime
                        endTimeError = null
                    }
                },
                readOnly = selectedTemplate != null,
                onFocusLost = { endTimeError = null },
                onFocused = { endTimeError = null },
                errorText = endTimeError
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverlayButton(text = "Отмена", onClick = onBack, modifier = Modifier.weight(1f))
                OverlayButton(
                    text = "Создать",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val parsedStart = parseDateTime(startTime.text.toString())
                        val parsedEnd   = parseDateTime(endTime.text.toString())
                        if (validate() && parsedStart != null && parsedEnd != null) {
                            controller.createEvent(
                                Event(
                                    id = null,
                                    user = null,
                                    template = selectedTemplate,
                                    name = name.text.toString().trim(),
                                    description = description.text.toString().trim().takeIf { it.isNotBlank() },
                                    image = selectedTemplate?.image,
                                    startTime = parsedStart,
                                    endTime = parsedEnd
                                )
                            )
                        }
                    }
                )
            }
        }

        // ── Modals over the form ──────────────────────────────────────────────
        when (activeModal) {

            OverlayCreateModal.Template -> OverlayTemplatePickerModal(
                templates = templates,
                selectedTemplate = selectedTemplate,
                onDismiss = { activeModal = null },
                onSelect = { template ->
                    selectedTemplate = template
                    name.edit { replace(0, length, template?.let { "${it.name} - " } ?: "") }
                    updateEndFromTemplate(template)
                    activeModal = null
                }
            )

            OverlayCreateModal.StartDateTime -> OverlayDateTimePickerModal(
                title = "Дата и время начала",
                dateText = startDateText,
                timeText = startTimeText,
                onDismiss = { activeModal = null },
                onDateClick = { activeModal = OverlayCreateModal.StartDate },
                onTimeClick = { activeModal = OverlayCreateModal.StartTime },
                onConfirm = {
                    updateField(startTime, startDateText, startTimeText)
                    if (selectedTemplate != null) updateEndFromTemplate()
                    startTimeError = null
                    activeModal = null
                }
            )

            OverlayCreateModal.StartDate -> OverlayDatePickerModal(
                title = "Дата начала",
                initialDate = startDateText,
                onDismiss = { activeModal = OverlayCreateModal.StartDateTime },
                onConfirm = { v -> startDateText = v; activeModal = OverlayCreateModal.StartDateTime }
            )

            OverlayCreateModal.StartTime -> OverlayTimePickerModal(
                title = "Время начала",
                initialTime = startTimeText,
                onDismiss = { activeModal = OverlayCreateModal.StartDateTime },
                onConfirm = { v -> startTimeText = v; activeModal = OverlayCreateModal.StartDateTime }
            )

            OverlayCreateModal.EndDateTime -> OverlayDateTimePickerModal(
                title = "Дата и время окончания",
                dateText = endDateText,
                timeText = endTimeText,
                onDismiss = { activeModal = null },
                onDateClick = { activeModal = OverlayCreateModal.EndDate },
                onTimeClick = { activeModal = OverlayCreateModal.EndTime },
                onConfirm = {
                    updateField(endTime, endDateText, endTimeText)
                    endTimeError = null
                    activeModal = null
                }
            )

            OverlayCreateModal.EndDate -> OverlayDatePickerModal(
                title = "Дата окончания",
                initialDate = endDateText,
                onDismiss = { activeModal = OverlayCreateModal.EndDateTime },
                onConfirm = { v -> endDateText = v; activeModal = OverlayCreateModal.EndDateTime }
            )

            OverlayCreateModal.EndTime -> OverlayTimePickerModal(
                title = "Время окончания",
                initialTime = endTimeText,
                onDismiss = { activeModal = OverlayCreateModal.EndDateTime },
                onConfirm = { v -> endTimeText = v; activeModal = OverlayCreateModal.EndDateTime }
            )

            null -> Unit
        }
    }
}

package com.example.kotlinclient.presentation.overlay

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.kotlinclient.presentation.utility.getStringTimeByDuration
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.ui.theme.Typography
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// DateTimePicker  —  промежуточное окно с выбором даты и времени (два ряда)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OverlayDateTimePickerModal(
    title: String,
    dateText: String,
    timeText: String,
    onDismiss: () -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onConfirm: () -> Unit
) {
    OverlayModalSurface(onOutsideClick = onDismiss) {

        Text(
            text = title,
            style = TextStyle(fontSize = Typography.titleMedium.fontSize, fontWeight = FontWeight.Bold),
            color = colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        // ── Строка выбора даты ────────────────────────────────────────────────
        Text(
            "Дата",
            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(colorScheme.surface, RoundedCornerShape(10.dp))
                .border(2.dp, colorScheme.outline, RoundedCornerShape(10.dp))
                .clickable(onClick = onDateClick)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = dateText.ifBlank { "Не выбрано" },
                style = Typography.bodyLarge.copy(
                    color = if (dateText.isBlank()) colorScheme.secondary else colorScheme.primary
                )
            )
            Image(
                painter = painterResource(R.drawable.pencil),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(colorScheme.secondary)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Строка выбора времени ─────────────────────────────────────────────
        Text(
            "Время",
            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(colorScheme.surface, RoundedCornerShape(10.dp))
                .border(2.dp, colorScheme.outline, RoundedCornerShape(10.dp))
                .clickable(onClick = onTimeClick)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = timeText.ifBlank { "Не выбрано" },
                style = Typography.bodyLarge.copy(
                    color = if (timeText.isBlank()) colorScheme.secondary else colorScheme.primary
                )
            )
            Image(
                painter = painterResource(R.drawable.pencil),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(colorScheme.secondary)
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OverlayButton(text = "Отмена", onClick = onDismiss, modifier = Modifier.weight(1f))
            OverlayButton(
                text = "Сохранить",
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                enabled = dateText.isNotBlank() && timeText.isNotBlank()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TemplatePicker
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OverlayTemplatePickerModal(
    templates: List<EventTemplate>,
    selectedTemplate: EventTemplate?,
    onDismiss: () -> Unit,
    onSelect: (EventTemplate?) -> Unit
) {
    // LazyColumn внутри — нельзя оборачивать во verticalScroll.
    OverlayModalSurface(scrollable = false, onOutsideClick = onDismiss) {

        Text(
            text = "Выбор шаблона",
            style = TextStyle(fontSize = Typography.titleMedium.fontSize, fontWeight = FontWeight.Bold),
            color = colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        if (templates.isEmpty()) {
            Text("Шаблонов пока нет", style = Typography.bodyMedium, color = colorScheme.secondary)
        } else {
            // Высота списка: 30% высоты экрана, зажатая в [160, 260] dp
            // — не раздувается на широких телефонах, но достаточна на маленьких
            val listMaxHeight = (LocalConfiguration.current.screenHeightDp * 0.30f).dp
                .coerceIn(160.dp, 260.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = listMaxHeight),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates.size) { index ->
                    val template = templates[index]
                    val isSelected = selectedTemplate?.id == template.id

                    // Текст длительности — повторяем логику из EventCreateModal
                    val durationText = when {
                        template.duration < 60_000L             -> "Меньше минуты"
                        template.duration > 86_400_000L * 30    -> "Больше месяца"
                        else                                    -> getStringTimeByDuration(template.duration)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) colorScheme.tertiaryContainer else colorScheme.surface,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colorScheme.tertiary else colorScheme.outline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelect(template) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LocalImage(template.image, Modifier.size(44.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = template.name,
                                style = TextStyle(
                                    fontSize = Typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colorScheme.primary,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2
                            )
                            Text(
                                text = durationText,
                                style = TextStyle(
                                    fontSize = Typography.bodySmall.fontSize,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = colorScheme.secondary,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OverlayButton(text = "Сбросить", onClick = { onSelect(null) }, modifier = Modifier.weight(1f))
            OverlayButton(text = "Отмена",   onClick = onDismiss,          modifier = Modifier.weight(1f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DatePicker
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayDatePickerModal(
    title: String,
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val dateFormat    = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    val initialMillis = initialDate
        .takeIf { it.isNotBlank() }
        ?.let { runCatching { java.time.LocalDate.parse(it, dateFormatter) }.getOrNull() }
        ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    val config      = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    // В landscape переключаемся в режим ввода: компактные текстовые поля вместо
    // полного календаря. Это единственный надёжный способ уместить DatePicker
    // на экране при маленькой высоте (≈360 dp).
    // В portrait стартуем с полного календаря — он целиком влезает в 420 dp.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        initialDisplayMode = if (isLandscape) DisplayMode.Input else DisplayMode.Picker
    )

    // scrollable = true безопасен, т.к. в portrait DatePicker ограничен height(420.dp)
    // — LazyVerticalGrid получает конечное ограничение и не падает.
    // В landscape DatePicker.Input не содержит LazyVerticalGrid вообще.
    OverlayModalSurface(maxWidth = 520, scrollable = true) {

        Text(
            text = title,
            style = TextStyle(fontSize = Typography.titleMedium.fontSize, fontWeight = FontWeight.Bold),
            color = colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        DatePicker(
            state = datePickerState,
            // Показываем переключатель режимов: пользователь сам выбирает
            // календарь или текстовый ввод (особенно полезно в landscape).
            showModeToggle = true,
            modifier = if (!isLandscape) Modifier.height(420.dp) else Modifier
        )

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OverlayButton(text = "Отмена", onClick = onDismiss, modifier = Modifier.weight(1f))
            OverlayButton(
                text = "Далее",
                modifier = Modifier.weight(1f),
                enabled = datePickerState.selectedDateMillis != null,
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onConfirm(dateFormat.format(Date(millis)))
                    }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TimePicker
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayTimePickerModal(
    title: String,
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val initial = initialTime
        .takeIf { it.isNotBlank() }
        ?.let { runCatching { LocalTime.parse(it, timeFormatter) }.getOrNull() }
        ?: LocalTime.now()

    val timePickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true
    )

    OverlayModalSurface(maxWidth = 420, onOutsideClick = onDismiss) {

        Text(
            text = title,
            style = TextStyle(fontSize = Typography.titleMedium.fontSize, fontWeight = FontWeight.Bold),
            color = colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TimeInput(state = timePickerState)
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OverlayButton(text = "Отмена",    onClick = onDismiss, modifier = Modifier.weight(1f))
            OverlayButton(
                text = "Сохранить",
                modifier = Modifier.weight(1f),
                onClick = {
                    onConfirm(String.format("%02d:%02d", timePickerState.hour, timePickerState.minute))
                }
            )
        }
    }
}

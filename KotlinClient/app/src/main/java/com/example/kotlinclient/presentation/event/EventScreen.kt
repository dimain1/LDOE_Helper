package com.example.kotlinclient.presentation.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.presentation.event.modal.EventCreateModal
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.viewModel.DialogType
import com.example.kotlinclient.state_management.viewModel.EventAction
import com.example.kotlinclient.state_management.viewModel.EventDialogType
import com.example.kotlinclient.state_management.viewModel.EventFormAction
import com.example.kotlinclient.state_management.viewModel.EventFormFields
import com.example.kotlinclient.state_management.viewModel.EventUiState
import com.example.kotlinclient.state_management.viewModel.SharedAction
import com.example.kotlinclient.state_management.viewModel.SharedUiState
import com.example.kotlinclient.ui.theme.Typography
import java.time.LocalDateTime

@Composable
fun EventScreen(
    uiState: EventUiState,
    onAction: (EventAction) -> Unit,
    templates: List<EventTemplate>,
    eventFormFields: EventFormFields,
    onFormAction: (EventFormAction) -> Unit,
    sharedAppUiState: SharedUiState,
    onSharedAction: (SharedAction) -> Unit,
    currentTime: LocalDateTime,
    paddingValues: PaddingValues
) {

    when (uiState.activeDialog) {
        is EventDialogType.Create -> {

            EventCreateModal(
                onDismiss = { onAction(EventAction.DismissDialog) },
                templates = templates,
                eventFormFields = eventFormFields,
                onFormAction = onFormAction,
            )
        }


        is EventDialogType.Edit -> {
            EventCreateModal(
                onDismiss = { onAction(EventAction.DismissDialog) },
                templates = templates,
                eventFormFields = eventFormFields,
                onFormAction = onFormAction,
            )
        }

        is EventDialogType.View -> {
            onSharedAction(SharedAction.ChangeDialogVisibility(true ,DialogType.EventViewDetailsDialog(uiState.activeDialog.eventData)))
        }

        else -> {}
    }

    // Контейнер всего экрана
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
    {
        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
        // Основной контейнер экрана
        Column(
            modifier = Modifier
                .background(color = colorScheme.secondaryContainer)
                .fillMaxWidth()
                .weight(1f)

        )
        {
            Spacer(Modifier.height(16.dp))
            // Контейнер верхней части(Заголовок + Кнопка)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            {
                // Заголовок экрана
                Text(
                    text = "Event Notifications",
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary
                )
                // Кнопка экрана(Создание ивента)
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.tertiary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(25),
                    modifier = Modifier,
                    onClick = {
                        onFormAction(EventFormAction.ClearUiState)
                        onAction(EventAction.OpenDialog(EventDialogType.Create))
                    }
                )
                {
                    // Иконка внутри кнопки
                    Icon(Icons.Default.Add, "Add", tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    // Текст внутри кнопки
                    Text(
                        text = "Create Event",
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)

            Spacer(Modifier.height(16.dp))

            // Список ивентов
            EventList(
                uiState.events,
                200,
                { id -> onAction(EventAction.DeleteEvent(id)) },
                { event ->
                    onFormAction(EventFormAction.LoadUiState(event))
                    onAction(EventAction.OpenDialog(EventDialogType.Edit))
                },
                onEventClick = { event -> onAction(EventAction.OpenDialog(EventDialogType.View(event))) },
                currentTime = currentTime
            )

        }

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
    }

}
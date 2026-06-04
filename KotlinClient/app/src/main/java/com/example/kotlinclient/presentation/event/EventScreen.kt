package com.example.kotlinclient.presentation.event

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.kotlinclient.presentation.event.modal.EventCreateModal
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import org.koin.compose.koinInject
import com.example.kotlinclient.state_management.viewModel.DialogType
import com.example.kotlinclient.state_management.viewModel.EventAction
import com.example.kotlinclient.state_management.viewModel.EventDialogType
import com.example.kotlinclient.state_management.viewModel.EventFormAction
import com.example.kotlinclient.state_management.viewModel.EventFormUiState
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
    eventFormFields: EventFormUiState,
    onFormAction: (EventFormAction) -> Unit,
    currentTime: LocalDateTime,
    paddingValues: PaddingValues
) {

    val context: Context = LocalContext.current
    val session: UserSessionProvider = koinInject()
    val user by session.user.collectAsState()

    when (uiState.activeDialog) {
        is EventDialogType.Create -> {

            EventCreateModal(
                onDismiss = {
                    onAction(EventAction.DismissDialog)
                    onFormAction(EventFormAction.ClearUiState)
                },
                templates = templates,
                eventFormFields = eventFormFields,
                onFormAction = onFormAction,
            )
        }


        is EventDialogType.Edit -> {
            EventCreateModal(
                onDismiss = {
                    onAction(EventAction.DismissDialog)
                    onFormAction(EventFormAction.ClearUiState)
                },
                templates = templates,
                eventFormFields = eventFormFields,
                onFormAction = onFormAction,
            )
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
                    text = "Уведомления о событиях",
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary
                )

                Spacer(Modifier.width(16.dp))

                // Кнопка экрана(Создание ивента)
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.tertiary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(25),
                    onClick = {
                        if (user == null) {
                            Toast.makeText(
                                context,
                                "Войдите в аккаунт, чтобы создавать события",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (checkAlarmPermission(context)) {
                            onFormAction(EventFormAction.ClearUiState)
                            onAction(EventAction.OpenDialog(EventDialogType.Create))
                        }
                    }
                )
                {
                    // Иконка внутри кнопки
                    Icon(Icons.Default.Add, "Add", tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    // Текст внутри кнопки
                    Text(
                        text = "Создать событие",
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

fun checkAlarmPermission(context: Context): Boolean{

    val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            }
            context.startActivity(intent)
            return false
        }
        return true
    }
    return true
}
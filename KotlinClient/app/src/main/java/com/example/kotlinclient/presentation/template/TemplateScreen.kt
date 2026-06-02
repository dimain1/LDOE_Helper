package com.example.kotlinclient.presentation.template

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.presentation.template.modal.TemplateCreateModal
import com.example.kotlinclient.presentation.template.modal.TemplateViewDetails
import com.example.kotlinclient.state_management.viewModel.EventFormAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateUiState
import com.example.kotlinclient.state_management.viewModel.EventTemplateDialogType
import com.example.kotlinclient.state_management.viewModel.EventTemplateFormAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateFormUiState
import com.example.kotlinclient.ui.theme.Typography

@Composable
fun TemplateScreen(
    uiState: EventTemplateUiState,
    onAction: (EventTemplateAction) -> Unit,
    formUiState: EventTemplateFormUiState,
    onFormAction: (EventTemplateFormAction) -> Unit,
    paddingValues: PaddingValues
) {


    when (uiState.activeDialog) {
        is EventTemplateDialogType.Create ->{

            TemplateCreateModal(
            formUiState,
            onFormAction,
            {
                onAction(EventTemplateAction.DismissDialog)
                    onFormAction(EventTemplateFormAction.ClearUiState)
            })
        }
        is EventTemplateDialogType.Edit -> {

            TemplateCreateModal(
            formUiState,
            onFormAction,
            { onAction(EventTemplateAction.DismissDialog)
                onFormAction(EventTemplateFormAction.ClearUiState)
            })
        }
        is EventTemplateDialogType.View -> {

            TemplateViewDetails(
                onAction,
                onDismiss = {onAction(EventTemplateAction.DismissDialog)},
                initialData = uiState.activeDialog.initialData
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
                    text = "Шаблоны событий",
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
                        onFormAction(EventTemplateFormAction.ClearUiState)
                        onAction(EventTemplateAction.OpenDialog(EventTemplateDialogType.Create))
                    }
                )
                {
                    // Иконка внутри кнопки
                    Icon(Icons.Default.Add, "Add", tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    // Текст внутри кнопки
                    Text(
                        text = "Новый шаблон",
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)

            Spacer(Modifier.height(16.dp))

            // Список ивентов
            TemplateList(
                uiState.templates,
                200,
                onAction
            )

        }

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
    }

}
package com.example.kotlinclient.presentation.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.presentation.utility.uiComponent.BasicTextFieldInModal
import com.example.kotlinclient.presentation.utility.uiComponent.ValidatedBasicTextFieldInModal
import com.example.kotlinclient.state_management.viewModel.SettingsAction
import com.example.kotlinclient.state_management.viewModel.SettingsDialogType
import com.example.kotlinclient.state_management.viewModel.SettingsFormAction
import com.example.kotlinclient.state_management.viewModel.SettingsFormUiState
import com.example.kotlinclient.state_management.viewModel.SettingsFormValidation
import com.example.kotlinclient.state_management.viewModel.SettingsUiState
import com.example.kotlinclient.ui.theme.Typography

@Composable
fun SettingsModalDialog(
    uiState: SettingsUiState,
    formUiState: SettingsFormUiState,
    onDismiss: () -> Unit,
    onAction: (SettingsAction) -> Unit
) {

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (uiState.activeDialog) {
                        is SettingsDialogType.EditProfile -> "Редакитровать профиль"
                        is SettingsDialogType.Registration -> "Регистрация"
                        is SettingsDialogType.Authorization -> "Авторизация"
                        else -> ""
                    },
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column() {
                when (uiState.activeDialog) {
                    is SettingsDialogType.EditProfile -> {


                        BasicTextFieldInModal(
                            formUiState.login,
                            "Имя пользователя",
                            readOnly = true
                        )
                        Spacer(Modifier.height(16.dp))
                        BasicTextFieldInModal(formUiState.email, "Электронная почта")
                    }

                    is SettingsDialogType.Registration -> {
                        BasicTextFieldInModal(formUiState.login, "Имя пользователя")
                        Spacer(Modifier.height(16.dp))
                        BasicTextFieldInModal(formUiState.email, "Электронная почта")
                        Spacer(Modifier.height(16.dp))
                        BasicTextFieldInModal(
                            formUiState.password,
                            "Пароль",
                            keyboardOption = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        Spacer(Modifier.height(16.dp))
                        BasicTextFieldInModal(
                            formUiState.confirmPassword,
                            "Подтвердите пароль",
                            keyboardOption = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }

                    is SettingsDialogType.Authorization -> {
                        BasicTextFieldInModal(formUiState.login, "Имя пользователя")
                        Spacer(Modifier.height(16.dp))
                        BasicTextFieldInModal(
                            formUiState.password,
                            "Пароль",
                            keyboardOption = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }

                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (uiState.activeDialog) {
                        SettingsDialogType.Authorization -> onAction(
                            SettingsAction.OnFormAction(
                                SettingsFormAction.AuthorizationUser(context)
                            )
                        )

                        SettingsDialogType.EditProfile -> onAction(
                            SettingsAction.OnFormAction(
                                SettingsFormAction.EditUserInfo(context)
                            )
                        )

                        SettingsDialogType.Registration -> onAction(
                            SettingsAction.OnFormAction(
                                SettingsFormAction.Registration(context)
                            )
                        )

                        null -> {}
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.tertiary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = when (uiState.activeDialog) {
                        SettingsDialogType.Authorization -> "Войти"
                        SettingsDialogType.EditProfile -> "Изменить"
                        SettingsDialogType.Registration -> "Создать"
                        null -> ""
                    }, color = Color.White
                )
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss()
                          },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.tertiary,
                    contentColor = Color.White
                )
            ) {
                Text("Отменить", color = Color.White)
            }
        },
        containerColor = colorScheme.secondaryContainer

    )

}
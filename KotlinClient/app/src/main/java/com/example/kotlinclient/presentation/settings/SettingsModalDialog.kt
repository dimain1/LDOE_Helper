package com.example.kotlinclient.presentation.settings

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
                    .then(clearFocusModifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (uiState.activeDialog) {
                        is SettingsDialogType.EditProfile    -> "Редактировать профиль"
                        is SettingsDialogType.Registration   -> "Регистрация"
                        is SettingsDialogType.Authorization  -> "Авторизация"
                        is SettingsDialogType.ChangePassword -> "Сменить пароль"
                        else -> ""
                    },
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(modalFocusRequester)
                    .focusable()
                    .then(clearFocusModifier)
            ) {
                when (uiState.activeDialog) {
                    is SettingsDialogType.EditProfile -> {
                        ValidatedBasicTextFieldInModal(
                            state = formUiState.login,
                            errorText = formUiState.errors.loginError,
                            onFocused = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ClearLoginError
                                        )
                                    )
                                )
                            },
                            onFocusLost = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ValidateLogin
                                        )
                                    )
                                )
                            },
                            placeholder = "Имя пользователя",
                            readOnly = true
                        )
                        Spacer(Modifier.height(16.dp))
                        ValidatedBasicTextFieldInModal(
                            state = formUiState.email,
                            errorText = formUiState.errors.emailError,
                            onFocused = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ClearEmailError
                                        )
                                    )
                                )
                            },
                            onFocusLost = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ValidateEmail
                                        )
                                    )
                                )
                            },
                            placeholder = "Электронная почта",
                        )
                    }

                    is SettingsDialogType.Registration -> {
                        ValidatedBasicTextFieldInModal(
                            state = formUiState.login,
                            errorText = formUiState.errors.loginError,
                            onFocused = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ClearLoginError
                                        )
                                    )
                                )
                            },
                            onFocusLost = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ValidateLogin
                                        )
                                    )
                                )
                            },
                            placeholder = "Имя пользователя",
                        )
                        Spacer(Modifier.height(16.dp))
                        ValidatedBasicTextFieldInModal(
                            state = formUiState.email,
                            errorText = formUiState.errors.emailError,
                            onFocused = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ClearEmailError
                                        )
                                    )
                                )
                            },
                            onFocusLost = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ValidateEmail
                                        )
                                    )
                                )
                            },
                            placeholder = "Электронная почта",
                        )
                        Spacer(Modifier.height(16.dp))
                        ValidatedBasicTextFieldInModal(
                            state = formUiState.password,
                            errorText = formUiState.errors.passwordError,
                            onFocused = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ClearPasswordError
                                        )
                                    )
                                )
                            },
                            onFocusLost = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ValidatePassword
                                        )
                                    )
                                )
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ValidateConfirmPassword
                                        )
                                    )
                                )
                            },
                            placeholder = "Пароль",
                            keyboardOption = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        Spacer(Modifier.height(16.dp))
                        ValidatedBasicTextFieldInModal(
                            state = formUiState.confirmPassword,
                            errorText = formUiState.errors.passwordConfirmError,
                            onFocused = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ClearConfirmPasswordError
                                        )
                                    )
                                )
                            },
                            onFocusLost = {
                                onAction(
                                    SettingsAction.OnFormAction(
                                        SettingsFormAction.OnFormValidation(
                                            SettingsFormValidation.ValidateConfirmPassword
                                        )
                                    )
                                )
                            },
                            placeholder = "Подтвердите пароль",
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

                    is SettingsDialogType.ChangePassword -> {
                        ValidatedBasicTextFieldInModal(
                            state = formUiState.oldPassword,
                            errorText = formUiState.errors.oldPasswordError,
                            onFocused = {},
                            onFocusLost = {},
                            placeholder = "Текущий пароль",
                            keyboardOption = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        Spacer(Modifier.height(16.dp))
                        ValidatedBasicTextFieldInModal(
                            state = formUiState.password,
                            errorText = formUiState.errors.passwordError,
                            onFocused = {
                                onAction(SettingsAction.OnFormAction(
                                    SettingsFormAction.OnFormValidation(SettingsFormValidation.ClearPasswordError)
                                ))
                            },
                            onFocusLost = {
                                onAction(SettingsAction.OnFormAction(
                                    SettingsFormAction.OnFormValidation(SettingsFormValidation.ValidatePassword)
                                ))
                            },
                            placeholder = "Новый пароль",
                            keyboardOption = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        Spacer(Modifier.height(16.dp))
                        ValidatedBasicTextFieldInModal(
                            state = formUiState.confirmPassword,
                            errorText = formUiState.errors.passwordConfirmError,
                            onFocused = {
                                onAction(SettingsAction.OnFormAction(
                                    SettingsFormAction.OnFormValidation(SettingsFormValidation.ClearConfirmPasswordError)
                                ))
                            },
                            onFocusLost = {
                                onAction(SettingsAction.OnFormAction(
                                    SettingsFormAction.OnFormValidation(SettingsFormValidation.ValidateConfirmPassword)
                                ))
                            },
                            placeholder = "Подтвердите новый пароль",
                            keyboardOption = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }

                    else -> {}
                }

                // Ошибка сервера — показывается в любом диалоге если есть
                formUiState.errors.serverError
                    ?.takeIf { it.isNotBlank() }
                    ?.let { error ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = colorScheme.tertiary,
                            style = Typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (uiState.activeDialog) {
                        SettingsDialogType.Authorization -> onAction(
                            SettingsAction.OnFormAction(
                                SettingsFormAction.AuthorizationUser
                            )
                        )

                        SettingsDialogType.EditProfile -> onAction(
                            SettingsAction.OnFormAction(
                                SettingsFormAction.EditUserInfo
                            )
                        )

                        SettingsDialogType.Registration -> onAction(
                            SettingsAction.OnFormAction(
                                SettingsFormAction.Registration
                            )
                        )

                        SettingsDialogType.ChangePassword -> onAction(
                            SettingsAction.OnFormAction(
                                SettingsFormAction.ChangePassword
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
                        SettingsDialogType.ChangePassword -> "Изменить"
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
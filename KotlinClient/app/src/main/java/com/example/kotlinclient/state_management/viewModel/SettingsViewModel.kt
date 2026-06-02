package com.example.kotlinclient.state_management.viewModel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.AuthRepository
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import com.example.kotlinclient.state_management.repository.interfaces.UserRepository
import com.example.kotlinclient.api_client.httpCode
import com.example.kotlinclient.api_client.isNetworkError
import com.example.kotlinclient.api_client.serverMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// region Settings screen ──────────────────────────────────────────────────────

data class SettingsUiState(
    val user: User? = null,
    val notification: Boolean = false,
    val sound: Boolean = false,
    val theme: Boolean = false,
    val activeDialog: SettingsDialogType? = null
)

sealed interface SettingsDialogType {
    data object Authorization : SettingsDialogType
    data object Registration : SettingsDialogType
    data object EditProfile : SettingsDialogType
    data object ChangePassword : SettingsDialogType
}

sealed interface SettingsAction {
    data class SwitchPreference(val key: String) : SettingsAction
    data object ExitProfile : SettingsAction
    data class OnFormAction(val action: SettingsFormAction) : SettingsAction
    data object DismissDialog : SettingsAction
    data class OpenDialog(val dialog: SettingsDialogType) : SettingsAction
}

data class SettingsFormUiState(
    val login: TextFieldState = TextFieldState(""),
    val email: TextFieldState = TextFieldState(""),
    val password: TextFieldState = TextFieldState(""),
    val confirmPassword: TextFieldState = TextFieldState(""),
    val oldPassword: TextFieldState = TextFieldState(""),
    val errors: SettingsFormErrors = SettingsFormErrors()
)

data class SettingsFormErrors(
    val loginError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmError: String? = null,
    val oldPasswordError: String? = null,
    val serverError: String? = null
)

sealed interface SettingsFormAction {
    data object LoadUiState : SettingsFormAction
    data object ClearUiState : SettingsFormAction
    data object EditUserInfo : SettingsFormAction
    data object AuthorizationUser : SettingsFormAction
    data object Registration : SettingsFormAction
    data object ChangePassword : SettingsFormAction
    data class OnFormValidation(val action: SettingsFormValidation) : SettingsFormAction
}

sealed interface SettingsFormValidation {
    data object ValidateLogin : SettingsFormValidation
    data object ClearLoginError : SettingsFormValidation
    data object ValidateEmail : SettingsFormValidation
    data object ClearEmailError : SettingsFormValidation
    data object ValidatePassword : SettingsFormValidation
    data object ClearPasswordError : SettingsFormValidation
    data object ValidateConfirmPassword : SettingsFormValidation
    data object ClearConfirmPasswordError : SettingsFormValidation
}

sealed interface SettingsFormNotificationEvent {
    data object SuccessUpdate : SettingsFormNotificationEvent
    data object SuccessAuth : SettingsFormNotificationEvent
    data object SuccessRegistration : SettingsFormNotificationEvent
    data object SuccessPasswordChange : SettingsFormNotificationEvent
    data object AuthFailed : SettingsFormNotificationEvent
    data class ServerError(val message: String) : SettingsFormNotificationEvent
}

// endregion ───────────────────────────────────────────────────────────────────

class SettingsViewModel(
    private val sharedPreferencesRepository: SharedPreferencesRepository,
    private val session: UserSessionProvider,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // ── Events ────────────────────────────────────────────────────────────────

    private val _notificationEvent = Channel<SettingsFormNotificationEvent>()
    val notificationEvent = _notificationEvent.receiveAsFlow()

    // ── UI state ──────────────────────────────────────────────────────────────

    private val _activeDialog = MutableStateFlow<SettingsDialogType?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        _activeDialog,
        sharedPreferencesRepository.observeBoolean("notification", false),
        sharedPreferencesRepository.observeBoolean("sound", false),
        sharedPreferencesRepository.observeBoolean("theme", false),
        sharedPreferencesRepository.observeLong("user_id", -1).flatMapLatest { id ->
            if (id != -1L) userRepository.getUserById(id) else flowOf(null)
        }
    ) { dialog, notification, sound, theme, user ->
        SettingsUiState(user, notification, sound, theme, dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    private val _formUiState = MutableStateFlow(SettingsFormUiState())
    val formUiState: StateFlow<SettingsFormUiState> = _formUiState.asStateFlow()

    // ── Actions ───────────────────────────────────────────────────────────────

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SwitchPreference -> switchPreference(action.key)
            is SettingsAction.ExitProfile      -> exitProfile()
            is SettingsAction.DismissDialog    -> dismissDialog()
            is SettingsAction.OpenDialog       -> openDialog(action.dialog)
            is SettingsAction.OnFormAction     -> onFormAction(action.action)
        }
    }

    fun onFormAction(action: SettingsFormAction) {
        when (action) {
            SettingsFormAction.LoadUiState         -> loadUiState()
            SettingsFormAction.ClearUiState        -> clearUiState()
            SettingsFormAction.AuthorizationUser   -> authorizationUser()
            SettingsFormAction.Registration        -> registration()
            SettingsFormAction.EditUserInfo        -> editUserInfo()
            SettingsFormAction.ChangePassword      -> changePassword()
            is SettingsFormAction.OnFormValidation -> onFormValidation(action.action)
        }
    }

    // ── Form actions ──────────────────────────────────────────────────────────

    private fun loadUiState() {
        val state = _formUiState.value
        val user  = uiState.value.user
        state.login.edit { replace(0, length, user?.login ?: "") }
        state.email.edit { replace(0, length, user?.email ?: "") }
    }

    private fun clearUiState() { _formUiState.value = SettingsFormUiState() }

    private fun authorizationUser() {
        val login    = _formUiState.value.login.text.toString().trim()
        val password = _formUiState.value.password.text.toString()
        if (!validateLoginField(login) || !validatePasswordField(password)) return

        viewModelScope.launch {
            authRepository.login(login, password)
                .onSuccess {
                    _notificationEvent.send(SettingsFormNotificationEvent.SuccessAuth)
                    dismissDialog()
                }
                .onFailure { e ->
                    when {
                        // Нет интернета / таймаут / DNS
                        e.isNetworkError() -> _notificationEvent.send(
                            SettingsFormNotificationEvent.ServerError("Нет подключения к интернету")
                        )
                        // Сервер вернул 401 — неверные креды
                        e.httpCode() == 401 -> {
                            _notificationEvent.send(SettingsFormNotificationEvent.AuthFailed)
                            _formUiState.value.password.edit { replace(0, length, "") }
                        }
                        // Любая другая ошибка сервера
                        else -> _notificationEvent.send(
                            SettingsFormNotificationEvent.ServerError(
                                e.serverMessage() ?: e.message ?: "Ошибка сервера"
                            )
                        )
                    }
                }
        }
    }

    private fun registration() {
        val login    = _formUiState.value.login.text.toString().trim()
        val email    = _formUiState.value.email.text.toString().trim()
        val password = _formUiState.value.password.text.toString()
        if (!validateLoginField(login) || !validateEmailFormat(email) ||
            !validatePasswordField(password) || !validateConfirmPasswordField()) return

        viewModelScope.launch {
            authRepository.register(login, email, password)
                .onSuccess {
                    _notificationEvent.send(SettingsFormNotificationEvent.SuccessRegistration)
                    dismissDialog()
                }
                .onFailure { e ->
                    val message = when {
                        e.isNetworkError()   -> "Нет подключения к интернету"
                        e.httpCode() == 409  -> "Логин или email уже заняты"
                        e.httpCode() == 400  -> "Проверьте введённые данные"
                        else -> e.serverMessage() ?: e.message ?: "Ошибка регистрации"
                    }
                    updateErrors { copy(serverError = message) }
                    _notificationEvent.send(SettingsFormNotificationEvent.ServerError(message))
                }
        }
    }

    private fun editUserInfo() {
        val login = _formUiState.value.login.text.toString().trim().takeIf { it.isNotBlank() }
        val email = _formUiState.value.email.text.toString().trim().takeIf { it.isNotBlank() }
        if (login != null && !validateLoginField(login)) return
        if (email != null && !validateEmailFormat(email)) return

        viewModelScope.launch {
            authRepository.updateProfile(login, email)
                .onSuccess {
                    _notificationEvent.send(SettingsFormNotificationEvent.SuccessUpdate)
                    dismissDialog()
                }
                .onFailure { e ->
                    val message = when {
                        e.isNetworkError()  -> "Нет подключения к интернету"
                        e.httpCode() == 409 -> "Логин или email уже заняты"
                        else -> e.serverMessage() ?: e.message ?: "Ошибка обновления"
                    }
                    updateErrors { copy(serverError = message) }
                    _notificationEvent.send(SettingsFormNotificationEvent.ServerError(message))
                }
        }
    }

    private fun changePassword() {
        val oldPassword = _formUiState.value.oldPassword.text.toString()
        val newPassword = _formUiState.value.password.text.toString()
        if (!validatePasswordField(newPassword) || !validateConfirmPasswordField()) return

        viewModelScope.launch {
            authRepository.changePassword(oldPassword, newPassword)
                .onSuccess {
                    _notificationEvent.send(SettingsFormNotificationEvent.SuccessPasswordChange)
                    dismissDialog()
                }
                .onFailure { e ->
                    when {
                        e.isNetworkError() -> _notificationEvent.send(
                            SettingsFormNotificationEvent.ServerError("Нет подключения к интернету")
                        )
                        e.httpCode() == 400 || e.httpCode() == 401 ->
                            updateErrors { copy(oldPasswordError = "Неверный текущий пароль") }
                        else -> _notificationEvent.send(
                            SettingsFormNotificationEvent.ServerError(
                                e.serverMessage() ?: e.message ?: "Ошибка смены пароля"
                            )
                        )
                    }
                }
        }
    }

    private fun switchPreference(key: String) {
        viewModelScope.launch { sharedPreferencesRepository.switchBooleanValueByKey(key) }
    }

    private fun exitProfile() {
        viewModelScope.launch { authRepository.logout() }
    }

    private fun openDialog(dialog: SettingsDialogType?) { _activeDialog.value = dialog }
    private fun dismissDialog() { _activeDialog.value = null }

    // ── Validation ────────────────────────────────────────────────────────────

    fun onFormValidation(action: SettingsFormValidation) {
        when (action) {
            SettingsFormValidation.ValidateLogin           -> validateLoginField(_formUiState.value.login.text.toString())
            SettingsFormValidation.ValidateEmail           -> validateEmailFormat(_formUiState.value.email.text.toString())
            SettingsFormValidation.ValidatePassword        -> validatePasswordField(_formUiState.value.password.text.toString())
            SettingsFormValidation.ValidateConfirmPassword -> validateConfirmPasswordField()
            SettingsFormValidation.ClearLoginError         -> updateErrors { copy(loginError = null) }
            SettingsFormValidation.ClearEmailError         -> updateErrors { copy(emailError = null) }
            SettingsFormValidation.ClearPasswordError      -> updateErrors { copy(passwordError = null) }
            SettingsFormValidation.ClearConfirmPasswordError -> updateErrors { copy(passwordConfirmError = null) }
        }
    }

    private fun validateLoginField(text: String): Boolean {
        val error = when {
            text.isBlank()    -> "Имя пользователя не может быть пустым"
            text.length <= 6  -> "Минимум 7 символов"
            text.length >= 40 -> "Слишком длинное имя пользователя"
            else              -> null
        }
        updateErrors { copy(loginError = error) }
        return error == null
    }

    private fun validateEmailFormat(text: String): Boolean {
        val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()
        val error = when {
            text.isBlank()            -> "Email не может быть пустым"
            !emailRegex.matches(text) -> "Неверный формат email"
            else                      -> null
        }
        updateErrors { copy(emailError = error) }
        return error == null
    }

    private fun validatePasswordField(text: String): Boolean {
        val error = when {
            text.isBlank()  -> "Пароль не может быть пустым"
            text.length < 8 -> "Минимум 8 символов"
            else            -> null
        }
        updateErrors { copy(passwordError = error) }
        return error == null
    }

    private fun validateConfirmPasswordField(): Boolean {
        val error = if (_formUiState.value.confirmPassword.text.toString() !=
            _formUiState.value.password.text.toString()
        ) "Пароли не совпадают" else null
        updateErrors { copy(passwordConfirmError = error) }
        return error == null
    }

    private fun updateErrors(transform: SettingsFormErrors.() -> SettingsFormErrors) {
        _formUiState.update { it.copy(errors = it.errors.transform()) }
    }
}

package com.example.kotlinclient.state_management.viewModel

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.presentation.utility.PasswordHasher
import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import com.example.kotlinclient.state_management.repository.interfaces.UserRepository
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

// region Settings screen

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
}

sealed interface SettingsAction {
    data class SwitchPreference(val key: String) : SettingsAction
    data class SetUserId(val id: Long) : SettingsAction
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
    val errors: SettingsFormErrors = SettingsFormErrors()
)

data class SettingsFormErrors(
    val loginError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmError: String? = null
)

sealed interface SettingsFormAction {
    data object LoadUiState : SettingsFormAction
    data object ClearUiState : SettingsFormAction
    data class EditUserInfo(val context: Context) : SettingsFormAction
    data class AuthorizationUser(val context: Context) : SettingsFormAction
    data class Registration(val context: Context) : SettingsFormAction
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
    data class SuccessUpdate(val context: Context) : SettingsFormNotificationEvent
    data class SuccessAuth(val context: Context) : SettingsFormNotificationEvent
    data class SuccessRegistration(val context: Context) : SettingsFormNotificationEvent
    data class AuthFailed(val context: Context) : SettingsFormNotificationEvent
}


// endregion

class SettingsViewModel(
    val sharedPreferencesRepository: SharedPreferencesRepository,
    val session: UserSessionProvider,
    val userRepository: UserRepository,
) : ViewModel() {

    // region flows

    private val _notificationEvent = Channel<SettingsFormNotificationEvent>()

    val notificationEvent = _notificationEvent.receiveAsFlow()


    private val _users = userRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _activeDialog = MutableStateFlow<SettingsDialogType?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        _activeDialog,
        sharedPreferencesRepository.observeBoolean("notification", false),
        sharedPreferencesRepository.observeBoolean("sound", false),
        sharedPreferencesRepository.observeBoolean("theme", false),
        sharedPreferencesRepository.observeLong("user_id", -1).flatMapLatest { id ->
            if (id != -1L) {
                userRepository.getUserById(id)
            } else {
                flowOf(null)
            }
        }
    ) { dialog, notification, sound, theme, user ->
        SettingsUiState(user, notification, sound, theme, dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    private val _formUiState = MutableStateFlow(SettingsFormUiState())

    val formUiState = _formUiState.asStateFlow()

    // endregion

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetUserId -> setUserId(action.id)
            is SettingsAction.SwitchPreference -> switchBooleanPreferences(action.key)
            is SettingsAction.ExitProfile -> exitProfile()
            is SettingsAction.DismissDialog -> dismissDialog()
            is SettingsAction.OpenDialog -> openDialog(action.dialog)
            is SettingsAction.OnFormAction -> onFormAction(action.action)

        }
    }

    // region onAction fuction

    private fun switchBooleanPreferences(key: String): Unit {
        viewModelScope.launch {
            sharedPreferencesRepository.switchBooleanValueByKey(key)
        }
    }

    private fun setUserId(id: Long) {
        viewModelScope.launch {
            sharedPreferencesRepository.putLongByKey("user_id", id)
        }
    }

    private fun exitProfile() {
        setUserId(-1)
    }

    private fun openDialog(dialog: SettingsDialogType?) {
        _activeDialog.value = dialog
    }

    private fun dismissDialog() {
        _activeDialog.value = null
    }

    // endregion

    fun onFormAction(action: SettingsFormAction) {
        when (action) {
            is SettingsFormAction.LoadUiState -> loadUiState()
            is SettingsFormAction.ClearUiState -> clearUiState()
            is SettingsFormAction.AuthorizationUser -> authorizationUser(action.context)
            is SettingsFormAction.EditUserInfo -> validateAndSave(action.context)
            is SettingsFormAction.Registration -> validateAndSave(action.context)
            is SettingsFormAction.OnFormValidation -> onFormValidation(action.action)
        }
    }

    // region onFormAction

    private fun loadUiState() {
        val state = _formUiState.value
        val user = uiState.value.user

        state.login.edit { replace(0, length, user?.login ?: "") }
        state.email.edit { replace(0, length, user?.email ?: "") }

    }

    private fun clearUiState() {
        _formUiState.value = SettingsFormUiState()
    }

    private fun isDataValid(): Boolean {
        return validateLogin() && validateEmail() && validatePassword() && validateConfirmPassword()
    }

    private fun validateAndSave(context: Context) {
        val formUiState = _formUiState.value
        val uiState = uiState.value

        viewModelScope.launch {


            val user = User(
                id = uiState.user?.id,
                login = formUiState.login.text.toString(),
                email = formUiState.email.text.toString(),
                password = PasswordHasher.hashPassword(formUiState.password.text.toString())
            )

            if (user.id == null) {
                if(isDataValid()){
                userRepository.createUser(user)
                _notificationEvent.send(SettingsFormNotificationEvent.SuccessRegistration(context))
                dismissDialog()
                }
            } else {
                if(validateLogin() && validateEmail()){
                    userRepository.updateUserInfo(user)
                    _notificationEvent.send(SettingsFormNotificationEvent.SuccessUpdate(context))
                    dismissDialog()
                }
            }


        }
    }

    private fun authorizationUser(context: Context) {
        val login = _formUiState.value.login.text.toString()
        val password = _formUiState.value.password.text.toString()

        viewModelScope.launch {

            val user: User? = _users.value.find { u ->
                (u.login == login)
                        && PasswordHasher.checkPassword(password, u.password)
            }

            if (user == null) {
                _notificationEvent.send(SettingsFormNotificationEvent.AuthFailed(context))
                _formUiState.value.password.edit { replace(0, length, "") }
            } else {
                setUserId(user.id!!)
                _notificationEvent.send(SettingsFormNotificationEvent.SuccessAuth(context))
                dismissDialog()
            }
        }
    }

    // endregion

    fun onFormValidation(action: SettingsFormValidation) {
        when (action) {
            SettingsFormValidation.ValidateConfirmPassword -> validateConfirmPassword()
            SettingsFormValidation.ValidateEmail -> validateEmail()
            SettingsFormValidation.ValidateLogin -> validateLogin()
            SettingsFormValidation.ValidatePassword -> validatePassword()
            SettingsFormValidation.ClearConfirmPasswordError -> updateErrors {
                copy(
                    passwordConfirmError = null
                )
            }

            SettingsFormValidation.ClearEmailError -> updateErrors { copy(emailError = null) }
            SettingsFormValidation.ClearLoginError -> updateErrors { copy(loginError = null) }
            SettingsFormValidation.ClearPasswordError -> updateErrors { copy(passwordError = null) }
        }
    }

    // region onFormValidation
    // "transform: SettingsFormErrors.() -> SettingsFormErrors
    //По сути transform Это лямбда функция, на вход получающая набор методов какого, то класса, и она обязывается возвращать экзмепляр этого класса?"
    private fun updateErrors(transform: SettingsFormErrors.() -> SettingsFormErrors) {
        _formUiState.update { it.copy(errors = it.errors.transform()) }
    }

    private fun validateLogin(): Boolean {
        val text = _formUiState.value.login.text.toString().trim()
        val error: String? = when {
            text.isBlank() -> "Имя пользователя не должно быть пустым"
            text.length <= 6 -> "Имя пользователя должно содержать больше 6 символов"
            text.length >= 40 -> "Имя пользователя слишком длинное"
            _users.value.find { user -> user.login == text } != null && _activeDialog.value == SettingsDialogType.Registration -> "Имя пользователя занято"
            else -> null
        }

        updateErrors { copy(loginError = error) }

        return error == null
    }

    private fun validateEmail(): Boolean {
        val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()

        val text = _formUiState.value.email.text.toString().lowercase().trim()
        val error: String? = when {
            text.isBlank() -> "Эл. почта должна быть заполнена"
            _users.value.find { user -> user.email == text } != null -> "Пользователь с такой Эл. почтой уже существует"
            !emailRegex.matches(text) -> "Формат почты не соответствует стандарту"
            else -> null
        }

        updateErrors { copy(emailError = error) }

        return error == null
    }

    private fun validatePassword(): Boolean {
        val text = _formUiState.value.password.text.toString().trim()
        val error: String? = when {
            text.isBlank() -> "Пароль не может быть пустым"
            text.length < 8 -> "Длина пароля должна быть не меньше 8 символов"
            else -> null
        }

        updateErrors { copy(passwordError = error) }

        return error == null
    }

    private fun validateConfirmPassword(): Boolean {
        val text = _formUiState.value.confirmPassword.text.toString().trim()
        val error: String? = when {
            text != _formUiState.value.password.text.toString() -> "Пароли не совпадают"
            else -> null
        }

        updateErrors { copy(passwordConfirmError = error) }

        return error == null
    }

    // endregion

    // region NotificationUser

    fun onNotification(event: SettingsFormNotificationEvent) {
        when (event) {
            is SettingsFormNotificationEvent.SuccessAuth -> Toast.makeText(
                event.context, "Успешная авторизация",
                Toast.LENGTH_SHORT
            ).show()

            is SettingsFormNotificationEvent.SuccessRegistration -> Toast.makeText(
                event.context, "Успешная регистрация",
                Toast.LENGTH_SHORT
            ).show()

            is SettingsFormNotificationEvent.SuccessUpdate -> Toast.makeText(
                event.context,
                "Профиль успешно изменён",
                Toast.LENGTH_SHORT
            ).show()

            is SettingsFormNotificationEvent.AuthFailed -> Toast.makeText(
                event.context, "Имя пользователя либо пароль неверны", Toast.LENGTH_SHORT
            ).show()

        }


    }

    // endregion

}
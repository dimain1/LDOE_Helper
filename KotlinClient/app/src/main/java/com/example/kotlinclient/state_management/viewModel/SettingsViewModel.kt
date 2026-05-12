package com.example.kotlinclient.state_management.viewModel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.repository.UserSession
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import com.example.kotlinclient.state_management.repository.interfaces.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.log

data class SettingsUiState(
    val user: User? = null,
    val notification: Boolean = false,
    val sound: Boolean = false,
    val theme: Boolean = false,
)

sealed interface SettingsAction{
    data class SwitchPreference(val key: String): SettingsAction
    data class SetUserId(val id: Long): SettingsAction
    data class EditUserInfo(val login: String, val email: String): SettingsAction
    data object ExitProfile: SettingsAction
}

class SettingsViewModel(
    val sharedPreferencesRepository: SharedPreferencesRepository,
    val session: UserSession,
    val userRepository: UserRepository,
): ViewModel() {

    val uiState:StateFlow<SettingsUiState> = combine(
        sharedPreferencesRepository.observeBoolean("notification",false),
        sharedPreferencesRepository.observeBoolean("sound",false),
        sharedPreferencesRepository.observeBoolean("theme",false),
        sharedPreferencesRepository.observeLong("user_id",-1).flatMapLatest {
            id ->
            if(id != -1L) {userRepository.getUserById(id)} else {flowOf(null)}
        }
    ){ notification, sound, theme, user ->
        SettingsUiState(user,notification,sound,theme)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())


//    private val _uiState = MutableStateFlow(SettingsUiState())
//
//    val uiState = _uiState.asStateFlow()
//
//    private val _notification = MutableStateFlow(false)
//    private val _sound = MutableStateFlow(false)
//    private val _theme = MutableStateFlow(false)
//    private val _userId = MutableStateFlow<Long?>(null)
//
//    init{
//        sharedPreferencesRepository.observeBoolean("notification", false).onEach {
//            notification -> _uiState.update { it.copy(notification = notification) }
//            _notification.value = notification
//        }.launchIn(viewModelScope)
//
//        sharedPreferencesRepository.observeBoolean("sound", false).onEach {
//                sound -> _uiState.update { it.copy(sound = sound) }
//            _sound.value = sound
//        }.launchIn(viewModelScope)
//
//        sharedPreferencesRepository.observeBoolean("theme", false).onEach {
//                theme -> _uiState.update { it.copy(theme = theme) }
//            _theme.value = theme
//        }.launchIn(viewModelScope)
//
//        sharedPreferencesRepository.observeLong("user_id", -1).flatMapLatest { id ->
//            _userId.value = id
//            if(id != -1L) {userRepository.getUserById(id)} else {flowOf(null)}
//        }.onEach {
//            user -> _uiState.update { it.copy(user=user) }
//        }.launchIn(viewModelScope)
//    }


    fun switchBooleanPreferences(key: String): Unit{
        viewModelScope.launch {
            sharedPreferencesRepository.switchBooleanValueByKey(key)
        }
    }

    fun setUserId(id: Long){
        viewModelScope.launch {
            sharedPreferencesRepository.putLongByKey("user_id", id)
        }
    }

    fun updateUserInfo(login: String, email:String){
        viewModelScope.launch {
            userRepository.updateUserInfo(session.requireId() , login,email)
        }
    }

    fun exitProfile(){
        setUserId(-1)
    }



}
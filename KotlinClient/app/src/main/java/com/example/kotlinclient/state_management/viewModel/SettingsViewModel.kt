package com.example.kotlinclient.state_management.viewModel

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import com.example.kotlinclient.state_management.repository.interfaces.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    val sharedPreferencesRepository: SharedPreferencesRepository,
    val userRepository: UserRepository
): ViewModel() {

    val notification: StateFlow<Boolean> = sharedPreferencesRepository.observeBoolean("notification", false).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val sound: StateFlow<Boolean> = sharedPreferencesRepository.observeBoolean("sound", false).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val theme: StateFlow<Boolean> = sharedPreferencesRepository.observeBoolean("theme", false).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val userId:StateFlow<Long> = sharedPreferencesRepository.observeLong("user_id", -1).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), -1
    )

    val user:StateFlow<User?> = userId.flatMapLatest {
        id -> userRepository.getUserById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)




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



}
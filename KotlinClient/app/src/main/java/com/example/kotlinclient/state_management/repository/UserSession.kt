package com.example.kotlinclient.state_management.repository

import com.example.kotlinclient.di.viewModelModule
import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import com.example.kotlinclient.state_management.repository.interfaces.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class UserSession(private val sharedPrefs: SharedPreferencesRepository, private val userRepository: UserRepository, private val externalScope: CoroutineScope) {
    val idFlow: Flow<Long> = sharedPrefs.observeLong("user_id", -1)

    val user: StateFlow<User?> = idFlow.flatMapLatest { id ->
        userRepository.getUserById(id)
    }.stateIn(externalScope, SharingStarted.Eagerly, null)

    suspend fun requireId(): Long = sharedPrefs.getLongByKey("user_id")

    fun <T> pipe(block: (Long) -> Flow<T>): Flow<T> {
        return idFlow.flatMapLatest { id ->
            block(id)
        }
    }
}
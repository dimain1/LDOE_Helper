package com.example.kotlinclient.state_management.repository

import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class UserSession(private val sharedPrefs: SharedPreferencesRepository) {
    val idFlow: Flow<Long> = sharedPrefs.observeLong("user_id", -1)

    suspend fun requireId(): Long = sharedPrefs.getLongByKey("user_id")

    fun <T> pipe(block: (Long) -> Flow<T>): Flow<T> {
        return idFlow.flatMapLatest { id ->
            block(id)
        }
    }
}
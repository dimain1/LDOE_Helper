package com.example.kotlinclient.state_management.repository

import com.example.kotlinclient.state_management.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserSessionProvider {

    val user: StateFlow<User?>

    suspend fun requireId(): Long

    fun <T> pipe(block: (Long) -> Flow<T>): Flow<T>

}
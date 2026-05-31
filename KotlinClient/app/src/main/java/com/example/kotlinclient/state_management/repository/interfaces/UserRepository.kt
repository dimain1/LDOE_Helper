package com.example.kotlinclient.state_management.repository.interfaces

import com.example.kotlinclient.state_management.entity.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getAllUsers(): Flow<List<User>>

    fun getUserById(id: Long) : Flow<User?>

    suspend fun updateUserInfo(user: User)

    suspend fun  deleteUser(user: User)

    suspend fun  createUser(user: User)


}
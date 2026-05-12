package com.example.kotlinclient.state_management.repository.interfaces

import com.example.kotlinclient.state_management.entity.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getUserById(id: Long) : Flow<User?>

    suspend fun updateUserInfo(id: Long,login: String, email:String)

}
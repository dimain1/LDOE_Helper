package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.local_cache.entity.UserEntity
import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.repository.UserSession
import com.example.kotlinclient.state_management.repository.interfaces.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.log

class UserRepositoryImpl(
    val database: AppDatabase,
    val session: UserSession
): UserRepository
{
    val userDao = database.UserDao()

    override fun getUserById(id: Long): Flow<User?> {
        return userDao.getUserById(id).map { userEntity -> userEntity?.toModel() }
    }

    override suspend fun updateUserInfo(login: String, email: String) {
        userDao.updateUserInfo(UserEntity(session.requireId(), login, email))
    }


}
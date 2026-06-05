package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toEntity
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.local_cache.entity.UserEntity
import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.repository.interfaces.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    database: AppDatabase,
): UserRepository
{
    val userDao = database.UserDao()
    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { list  -> list.map { userEntity -> userEntity.toModel() } }
    }

    override fun getUserById(id: Long): Flow<User?> {
        return userDao.getUserById(id).map { userEntity -> userEntity?.toModel() }
    }

    override suspend fun updateUserInfo(user: User) {
        userDao.updateUserInfo(user.toEntity())
    }

    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
    }

    override suspend fun createUser(user: User) {
        userDao.addNewUser(user.toEntity())
    }


}
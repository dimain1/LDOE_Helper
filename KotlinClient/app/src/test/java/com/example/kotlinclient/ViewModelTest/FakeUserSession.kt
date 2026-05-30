package com.example.kotlinclient.ViewModelTest

import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserSession(user:User?): UserSessionProvider {

    override val user = MutableStateFlow<User?>(user)


    override suspend fun requireId(): Long {
        return user.value?.id ?: -1
    }

    override fun <T> pipe(block: (Long) -> Flow<T>): Flow<T> {
        TODO("Not yet implemented")
    }

}
package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.entity.UserEntity
import com.example.kotlinclient.api_client.ApiService
import com.example.kotlinclient.api_client.TokenStorage
import com.example.kotlinclient.api_client.dto.ChangePasswordRequest
import com.example.kotlinclient.api_client.dto.LoginRequest
import com.example.kotlinclient.api_client.dto.RefreshRequest
import com.example.kotlinclient.api_client.dto.RegisterRequest
import com.example.kotlinclient.api_client.dto.UpdateUserRequest
import com.example.kotlinclient.presentation.utility.EventAlarmScheduler
import com.example.kotlinclient.state_management.repository.interfaces.AuthRepository
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository

class AuthRepositoryImpl(
    private val api: ApiService,
    private val tokenStorage: TokenStorage,
    private val sharedPrefs: SharedPreferencesRepository,
    private val alarmScheduler: EventAlarmScheduler,
    database: AppDatabase
) : AuthRepository {

    private val userDao  = database.UserDao()
    private val eventDao = database.EventDao()

    override suspend fun login(login: String, password: String): Result<Long> = runCatching {
        val authResponse = api.login(LoginRequest(login, password))
        tokenStorage.saveTokens(authResponse.accessToken, authResponse.refreshToken)

        val userDto = api.getMe()
        userDao.addNewUser(
            UserEntity(id = userDto.id, login = userDto.login, email = userDto.email, password = "")
        )
        sharedPrefs.putLongByKey("user_id", userDto.id)
        userDto.id
    }

    override suspend fun register(
        login: String,
        email: String,
        password: String
    ): Result<Long> = runCatching {
        val userName     = login
        val userEmail    = email
        val userPassword = password

        api.register(RegisterRequest(login = userName, password = userPassword, email = userEmail))
        this.login(userName, userPassword).getOrThrow()
    }

    override suspend fun logout() {
        // 1. Отменяем все аларм-таймеры текущего пользователя
        val userId = sharedPrefs.getLongByKey("user_id")
        if (userId != -1L) {
            eventDao.getAllEventIdsSync(userId)
                .filterNotNull()
                .forEach { localId -> alarmScheduler.cancel(localId) }
        }

        // 2. Инвалидируем refresh token на сервере (best-effort, ошибку игнорируем)
        tokenStorage.getRefreshToken()?.let { token ->
            runCatching { api.logout(RefreshRequest(token)) }
        }

        // 3. Чистим локальное состояние → UserSession реагирует реактивно
        tokenStorage.clear()
        sharedPrefs.putLongByKey("user_id", -1L)
    }

    override suspend fun updateProfile(login: String?, email: String?): Result<Unit> = runCatching {
        val dto = api.updateMe(UpdateUserRequest(login, email))
        userDao.updateUserInfo(
            UserEntity(id = dto.id, login = dto.login, email = dto.email, password = "")
        )
        sharedPrefs.putLongByKey("user_id", dto.id)
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> =
        runCatching {
            api.changePassword(ChangePasswordRequest(oldPassword, newPassword))
        }
}

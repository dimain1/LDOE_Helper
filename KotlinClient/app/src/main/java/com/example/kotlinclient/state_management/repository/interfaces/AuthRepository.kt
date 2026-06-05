package com.example.kotlinclient.state_management.repository.interfaces

interface AuthRepository {

    /**
     * Авторизация через сервер.
     * При успехе: сохраняет токены, сохраняет пользователя в Room, устанавливает user_id в SharedPrefs.
     * @return ID пользователя (из Room).
     */
    suspend fun login(login: String, password: String): Result<Long>

    /**
     * Регистрация на сервере + автоматический вход.
     * @return ID созданного пользователя.
     */
    suspend fun register(login: String, email: String, password: String): Result<Long>

    /**
     * Выход: инвалидирует refresh token на сервере, очищает токены и сессию локально.
     */
    suspend fun logout()

    /**
     * Обновить профиль (login / email) через PUT /users/me.
     * Обновляет Room после успешного ответа сервера.
     */
    suspend fun updateProfile(login: String?, email: String?): Result<Unit>

    /**
     * Сменить пароль через PUT /users/me/password.
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
}

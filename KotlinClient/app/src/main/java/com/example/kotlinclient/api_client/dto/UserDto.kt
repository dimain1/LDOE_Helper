package com.example.kotlinclient.api_client.dto

data class UserDto(
    val id: Long,
    val login: String,
    val email: String,
    val admin: Boolean
)

data class UpdateUserRequest(
    val login: String?,
    val email: String?
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

package com.example.kotlinclient.api_client.dto

data class LoginRequest(
    val login: String,
    val password: String
)

data class RegisterRequest(
    val login: String,
    val password: String,
    val email: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)

data class RefreshRequest(
    val refreshToken: String
)

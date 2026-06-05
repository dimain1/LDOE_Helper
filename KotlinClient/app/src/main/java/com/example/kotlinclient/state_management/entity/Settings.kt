package com.example.kotlinclient.state_management.entity

data class Settings(
    val id: Long,
    val preferences: Map<String, Any>
)

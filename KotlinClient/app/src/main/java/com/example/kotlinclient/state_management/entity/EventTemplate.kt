package com.example.kotlinclient.state_management.entity

data class EventTemplate(
    val id: Long?,
    val creator: User?,
    val name: String,
    val description: String?,
    val image: String?,
    val duration: Long
)

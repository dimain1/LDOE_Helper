package com.example.kotlinclient.state_management.entity

import java.time.LocalDateTime

data class Event(
    val id: Long?,
    val user: User?,
    val template: EventTemplate?,
    val name: String?,
    val description: String?,
    val image: String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

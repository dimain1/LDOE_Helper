package com.example.kotlinclient.state_management.entity

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class Event(
    val id: Long?,
    val user: User?,
    val template: EventTemplate?,
    val name: String?,
    val description: String?,
    val image: String?,
    val start_time: LocalDateTime,
    val end_time: LocalDateTime
)

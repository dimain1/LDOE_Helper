package com.example.kotlinclient.state_management.entity

import java.time.OffsetDateTime

data class Event(
    val id: Long,
    val user: User?,
    val template: EventTemplate?,
    val name: String?,
    val description: String?,
    val image: String?,
    val start_time: OffsetDateTime,
    val end_time: OffsetDateTime
)

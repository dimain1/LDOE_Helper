package com.example.kotlinclient.api_client.dto

data class EventDto(
    val id: Long,
    val userId: Long,
    val templateId: Long?,
    val name: String?,
    val description: String?,
    val imageUrl: String?,
    val startTime: String,   // ISO-8601 Instant → "2026-06-01T10:00:00Z"
    val endTime: String
)

data class EventCreateRequest(
    val templateId: Long?,
    val name: String?,
    val description: String?,
    val imageUrl: String?,
    val startTime: String,
    val endTime: String
)

data class EventUpdateRequest(
    val name: String?,
    val description: String?,
    val imageUrl: String?,
    val startTime: String?,
    val endTime: String?
)

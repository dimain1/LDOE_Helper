package com.example.kotlinclient.api_client.dto

data class EventDto(
    val id: Long,
    val userId: Long,
    val templateId: Long?,
    val name: String?,
    val description: String?,
    val imageUrl: String?,
    val startTime: String,
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

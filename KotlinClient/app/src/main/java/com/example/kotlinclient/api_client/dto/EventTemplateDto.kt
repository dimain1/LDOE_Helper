package com.example.kotlinclient.api_client.dto

data class EventTemplateDto(
    val id: Long,
    val creatorId: Long?,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val duration: Long
)

data class EventTemplateCreateRequest(
    val name: String,
    val description: String?,
    val duration: Long
)

data class EventTemplateUpdateRequest(
    val name: String?,
    val description: String?,
    val duration: Long?
)

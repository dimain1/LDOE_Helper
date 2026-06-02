package com.example.kotlinclient.api_client.dto

data class ContentTypeDto(
    val id: Long,
    val name: String
)

data class GameContentDto(
    val id: Long,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val attributes: Map<String, Any>?,
    val types: List<ContentTypeDto>,
    val pinned: Boolean
)

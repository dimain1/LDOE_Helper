package com.example.kotlinclient.state_management.entity

data class GameContent(
    val id: Long,
    val name: String,
    val description: String?,
    val image: String?,
    val pinned: Boolean,
    val types: Set<ContentType>?,
    val attributes: Map<String, Any>?

) {
}
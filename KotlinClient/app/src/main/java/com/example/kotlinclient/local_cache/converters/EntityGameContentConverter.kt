package com.example.kotlinclient.local_cache.converters

import com.example.kotlinclient.local_cache.entity.GameContentEntity
import com.example.kotlinclient.local_cache.entity.relationExtension.GameContentFull
import com.example.kotlinclient.local_cache.entity.relationExtension.GameContentWithTypes
import com.example.kotlinclient.state_management.entity.GameContent

fun GameContent.toEntity(): GameContentEntity {
    return GameContentEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        imageUrl = this.image,
        attributes = this.attributes,
    )
}

fun GameContentEntity.toModel(): GameContent {
    return GameContent(
        id = this.id,
        name = this.name,
        description = this.description,
        // Предпочитаем локальный файл (офлайн); fallback — серверный URL
        image = this.localImagePath ?: this.imageUrl,
        pinned = false,
        types = null,
        attributes = this.attributes
    )
}

fun GameContentFull.toModel(): GameContent {
    return GameContent(
        id = this.content.id,
        name = this.content.name,
        description = this.content.description,
        // Предпочитаем локальный файл (офлайн); fallback — серверный URL
        image = this.content.localImagePath ?: this.content.imageUrl,
        pinned = this.isPinned,
        types = this.types.map { type -> type.toModel() }.toSet(),
        attributes = this.content.attributes
    )
}

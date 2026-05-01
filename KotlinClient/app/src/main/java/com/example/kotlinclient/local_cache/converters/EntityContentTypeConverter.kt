package com.example.kotlinclient.local_cache.converters

import com.example.kotlinclient.local_cache.entity.ContentTypeEntity
import com.example.kotlinclient.state_management.entity.ContentType

fun ContentType.toEntity(): ContentTypeEntity{
    return ContentTypeEntity(
        id = this.id,
        name = this.name
    )
}

fun ContentTypeEntity.toModel(): ContentType{
    return ContentType(
        id = this.id,
        name = this.name
    )
}
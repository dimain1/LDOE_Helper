package com.example.kotlinclient.local_cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_content")
data class GameContentEntity(
    /** Используем server ID напрямую — контент только с сервера, локально не создаётся. */
    @PrimaryKey
    val id: Long,

    val name: String,
    val description: String?,

    @ColumnInfo(name = "image")
    val imageUrl: String?,

    val attributes: Map<String, Any>?
)

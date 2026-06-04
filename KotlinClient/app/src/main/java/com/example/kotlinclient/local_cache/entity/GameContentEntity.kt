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

    /** Серверный относительный URL (/images/xxx.jpg). */
    @ColumnInfo(name = "image")
    val imageUrl: String?,

    /** Абсолютный путь к локально скачанному файлу. Заполняется при syncFromServer. */
    @ColumnInfo(name = "local_image_path")
    val localImagePath: String? = null,

    val attributes: Map<String, Any>?
)

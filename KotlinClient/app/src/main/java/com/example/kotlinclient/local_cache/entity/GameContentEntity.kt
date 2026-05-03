package com.example.kotlinclient.local_cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(
    tableName = "game_content"
)
data class GameContentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val description: String?,
    @ColumnInfo(name="image")
    val imageUrl: String?,

    val attributes: Map<String, Any>?

)

package com.example.kotlinclient.local_cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "content_type",
    indices = [ Index(value= ["name"], unique = true) ]
)
data class ContentTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Long,

    @ColumnInfo
    val name: String

)

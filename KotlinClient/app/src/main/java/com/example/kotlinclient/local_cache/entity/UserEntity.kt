package com.example.kotlinclient.local_cache.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users",
    indices = [
        Index( value = ["login"], unique = true ),
        Index( value = ["email"], unique = true )
    ]

)
data class UserEntity(
    @PrimaryKey
    val id: Long? = null,
    val login: String,
    val email: String,
    val password: String

)

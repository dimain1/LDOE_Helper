package com.example.kotlinclient.local_cache.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "users",
    indices = [
        Index( value = ["login"], unique = true ),
        Index( value = ["email"], unique = true )
    ]

)
data class UserEntity(
    val id: Long,
    val login: String,
    val email: String,

)

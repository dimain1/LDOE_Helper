package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.relationExtension.TypeWithGameContent

@Dao
interface ContentTypeDao {

    @Transaction
    @Query("SELECT * FROM content_type")
    fun getAllTypesWithGameContent(): List<TypeWithGameContent>

}
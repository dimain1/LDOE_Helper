package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.ContentTypeEntity
import com.example.kotlinclient.local_cache.entity.relationExtension.TypeWithGameContent
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentTypeDao {

    @Transaction
    @Query("SELECT * FROM content_type")
    fun getAllTypesWithGameContent(): List<TypeWithGameContent>

    @Query("SELECT * FROM content_type")
    fun getAllTypes(): Flow<List<ContentTypeEntity>>

}
package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Query("SELECT * FROM content_type ORDER BY name")
    fun getAllTypes(): Flow<List<ContentTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertType(type: ContentTypeEntity)

    @Query("DELETE FROM content_type WHERE id = :id")
    suspend fun deleteType(id: Long)
}
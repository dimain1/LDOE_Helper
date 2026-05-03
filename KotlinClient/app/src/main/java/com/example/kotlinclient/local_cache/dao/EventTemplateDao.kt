package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.relationExtension.EventTemplateWithUser
import kotlinx.coroutines.flow.Flow

@Dao
interface EventTemplateDao {

    @Transaction
    @Query("SELECT * FROM event_template WHERE creator_id = :userId")
    fun getAllEventTemplateWithUser(userId: Long): Flow<List<EventTemplateWithUser>>

    @Query("DELETE FROM event_template WHERE id = :id AND creator_id = :userId")
    fun deleteTemplateById(userId: Long,id: Long)
}
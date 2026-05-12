package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.EventEntity
import com.example.kotlinclient.local_cache.entity.relationExtension.EventWithUserAndTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Transaction
    @Query("SELECT * FROM events WHERE end_time > :currentTimeMillis AND user_id = :userId ")
    fun getAllEventsUpcomingWithTemplate(userId: Long, currentTimeMillis: Long): Flow<List<EventWithUserAndTemplate>>

    @Transaction
    @Query("SELECT * FROM events WHERE user_id = :userId")
    fun getAllEventsWithTemplate(userId: Long): Flow<List<EventWithUserAndTemplate>>

    @Query("DELETE FROM events WHERE id = :id AND user_id = :userId")
    fun deleteEventById(userId: Long, id:Long)

    @Insert(EventEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun addEvent(event: EventEntity)

}
package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.relationExtension.EventWithUserAndTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Transaction
    @Query("SELECT * FROM events WHERE datetime(end_time) > datetime('now') ")
    fun getAllEventsUpcomingWithTemplate(): Flow<List<EventWithUserAndTemplate>>

    @Transaction
    @Query("SELECT * FROM events")
    fun getAllEventsWithTemplate(): Flow<List<EventWithUserAndTemplate>>

    @Query("DELETE FROM events WHERE id = :id")
    fun deleteEventById(id:Long)

}
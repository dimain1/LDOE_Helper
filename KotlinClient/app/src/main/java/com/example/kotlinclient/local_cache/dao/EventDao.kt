package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.relationExtension.EventWithUserAndTemplate

@Dao
interface EventDao {

    @Transaction
    @Query("SELECT * FROM events")
    fun getAllEventsWithTemplate(): List<EventWithUserAndTemplate>

}
package com.example.eventplancalender.dataLayer.dio

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eventplancalender.entity.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: Event)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("SELECT * FROM events WHERE date >= :startOfDay AND date < :endOfDay")
    fun getEventsByDate(startOfDay: Long, endOfDay: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE date >= :currentDate ORDER BY date ASC, time ASC")
    fun getUpcomingEvents(currentDate: Long): Flow<List<Event>>
}
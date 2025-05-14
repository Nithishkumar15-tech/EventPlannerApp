package com.example.eventplancalender.dataLayer.repository

import com.example.eventplancalender.dataLayer.dio.EventDao
import com.example.eventplancalender.entity.Event
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    suspend fun getEventsByDate(startOfDay: Long, endOfDay: Long): Flow<List<Event>> {
        return eventDao.getEventsByDate(startOfDay, endOfDay)
    }
    fun getUpcomingEvents(currentDate: Long): Flow<List<Event>> {
        return eventDao.getUpcomingEvents(currentDate)
    }

    suspend fun insert(event: Event) {
        eventDao.insert(event)
    }

    suspend fun update(event: Event) {
        eventDao.update(event)
    }

    suspend fun delete(event: Event) {
        eventDao.delete(event)
    }
}

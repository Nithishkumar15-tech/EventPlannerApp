package com.example.eventplancalender.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventplancalender.dataLayer.database.EventDatabase
import com.example.eventplancalender.dataLayer.repository.EventRepository
import com.example.eventplancalender.entity.Event
import com.example.eventplancalender.uiLayer.startOfDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class EventViewModel(private val repository: EventRepository) : ViewModel() {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _selectedDate = MutableStateFlow<Long>(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate
    private val _upcomingEvents = MutableStateFlow<List<Event>>(emptyList())
    val upcomingEvents: StateFlow<List<Event>> = _upcomingEvents
    fun setSelectedDate(date: Long) {
        _selectedDate.value = date
        loadEventsByDate(date)
    }

    fun loadUpcomingEvents() {
        viewModelScope.launch {
            val currentDate = System.currentTimeMillis().startOfDay()
            repository.getUpcomingEvents(currentDate).collect { events ->
                _upcomingEvents.value = events
            }
        }
    }


    fun loadEventsByDate(date: Long) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis

            repository.getEventsByDate(startOfDay, endOfDay).collect {
                _events.value = it
            }
        }
    }


    fun addEvent(title: String, description: String, date: Long) {
        viewModelScope.launch {
            val event = Event(title = title, description = description, date = date)
            Log.d("EventViewModel", "Adding event: $event") // Log the event before inserting
            repository.insert(event)
        }
    }


    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.update(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.delete(event)
        }
    }
}

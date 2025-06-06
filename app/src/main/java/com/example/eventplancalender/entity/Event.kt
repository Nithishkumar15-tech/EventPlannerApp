package com.example.eventplancalender.entity

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: Long,
    val time: String = "",

)

package com.example.eventplancalender.model

import java.time.LocalDateTime

data class ComposeCalendarEvent(
    val id: Int,
    val name: String,
    val color: androidx.compose.ui.graphics.Color,
    val textColor: androidx.compose.ui.graphics.Color,
    val start: LocalDateTime,
    val end: LocalDateTime
)


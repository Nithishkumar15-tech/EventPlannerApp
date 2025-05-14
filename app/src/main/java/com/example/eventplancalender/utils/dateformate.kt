package com.example.eventplancalender.utils
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

fun Long.isToday(): Boolean {
    val calendar = Calendar.getInstance().apply { timeInMillis = this@isToday }
    val today = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

fun Long.isTomorrow(): Boolean {
    val calendar = Calendar.getInstance().apply { timeInMillis = this@isTomorrow }
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    return calendar.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)
}
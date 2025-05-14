package com.example.eventplancalender.uiLayer

import android.os.Build
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.eventplancalender.entity.Event
import com.example.eventplancalender.model.ComposeCalendarEvent
import com.example.eventplancalender.viewmodel.EventViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomCalendar(
    events: List<Event>,
    upcomingEvents: List<Event>,
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    var currentMonth by remember { mutableStateOf(
        Instant.ofEpochMilli(selectedDate).atZone(ZoneId.systemDefault()).toLocalDate()
            .withDayOfMonth(1)
    ) }
    val today = LocalDate.now()
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.dayOfWeek.value % 7
    val allEvents = remember(events, upcomingEvents) {
        events + upcomingEvents
    }

    val eventDaysMap = remember(allEvents, currentMonth) {
        allEvents.mapNotNull { event ->
            val eventDate = Instant.ofEpochMilli(event.date).atZone(ZoneId.systemDefault()).toLocalDate()
            if (eventDate.year == currentMonth.year && eventDate.month == currentMonth.month) {
                val isUpcoming = upcomingEvents.any { it.date == event.date }
                eventDate.dayOfMonth to isUpcoming
            } else null
        }.groupBy({ it.first }, { it.second })
    }

    Column(modifier = Modifier.padding(8.dp)) {
        // Month navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { currentMonth = currentMonth.minusMonths(1) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous month")
            }

            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            IconButton(
                onClick = { currentMonth = currentMonth.plusMonths(1) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next month")
            }
        }

        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(240.dp)
        ) {
            // Blank days before the first of the month
            items(firstDayOfWeek) { Box(modifier = Modifier.padding(4.dp)) }

            // Days of the month
            items(daysInMonth) { day ->
                val dayOfMonth = day + 1
                val date = currentMonth.withDayOfMonth(dayOfMonth)
                val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val eventFlags = eventDaysMap[dayOfMonth] ?: emptyList()
                val hasEvent = eventFlags.isNotEmpty()
                val isUpcomingEvent = eventFlags.any { it }
                val isSelected = millis.startOfDay() == selectedDate.startOfDay()
                val isToday = date == today

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .background(
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else -> Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .clickable { onDateSelected(millis) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$dayOfMonth",
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (hasEvent) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isUpcomingEvent -> Color(0xFF4CAF50) // Green for upcoming
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}


fun Long.startOfDay(): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = this@startOfDay }
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
package com.example.eventplancalender.uiLayer

import android.app.DatePickerDialog
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventplancalender.entity.Event
import com.example.eventplancalender.model.ComposeCalendarEvent
import com.example.eventplancalender.utils.isToday
import com.example.eventplancalender.utils.isTomorrow
import com.example.eventplancalender.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    viewModel: EventViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Event) -> Unit
) {
    var showCalendar by remember { mutableStateOf(true) }
    val events by viewModel.events.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()

    val today = System.currentTimeMillis().startOfDay()
    // Initialize with current date and load events
    LaunchedEffect(Unit) {
        viewModel.setSelectedDate(today)
        viewModel.loadEventsByDate(today)
        viewModel.loadUpcomingEvents()
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.padding(start = 16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            .format(Date(selectedDate)),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { showCalendar = !showCalendar }) {
                        Icon(
                            imageVector = if (showCalendar) Icons.Default.List else Icons.Default.DateRange,
                            contentDescription = "Toggle View"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SelectedDateHeader(viewModel = viewModel)

            AnimatedVisibility(
                visible = showCalendar,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CustomCalendar(
                    events = events,
                    selectedDate = selectedDate,
                    upcomingEvents = upcomingEvents,

                    onDateSelected = { date ->
                        viewModel.setSelectedDate(date)
                        viewModel.loadEventsByDate(date)
                    }
                )
            }

            AnimatedVisibility(
                visible = !showCalendar,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                MainEventList(viewModel = viewModel, onEditClick = onEditClick)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Events for selected date",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            key(selectedDate) {
                EventList(
                    events = events,
                    selectedDate = selectedDate,
                    onEditClick = onEditClick
                )
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventList(
    events: List<Event>,
    selectedDate: Long,
    onEditClick: (Event) -> Unit
) {
    val selectedDayStart = Calendar.getInstance().apply {
        timeInMillis = selectedDate
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val selectedDayEnd = selectedDayStart + 24 * 60 * 60 * 1000

    val filteredEvents = events.filter { it.date in selectedDayStart until selectedDayEnd }

    if (filteredEvents.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(filteredEvents) { event ->
                EventItem(event = event, onEditClick = onEditClick)
            }
        }
    }
}

@Composable
fun SelectedDateHeader(viewModel: EventViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val context = LocalContext.current

    val dateFormatter = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val newDate = Calendar.getInstance().apply {
                                set(year, month, day, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            viewModel.setSelectedDate(newDate)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = dateFormatter.format(Date(selectedDate)),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (selectedDate.isToday()) "Today"
                    else if (selectedDate.isTomorrow()) "Tomorrow"
                    else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(Icons.Default.Edit, contentDescription = "Change date")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainEventList(
    viewModel: EventViewModel,
    onEditClick: (Event) -> Unit
) {
    val events by viewModel.events.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val upcomingEvents = remember(events) {
        val now = System.currentTimeMillis()
        val sevenDaysLater = now + 7 * 24 * 60 * 60 * 1000
        events.filter { it.date in now..sevenDaysLater }
            .sortedBy { it.date }
            .groupBy {
                val calendar = Calendar.getInstance().apply { timeInMillis = it.date }
                calendar.get(Calendar.DAY_OF_YEAR) to calendar.get(Calendar.YEAR)
            }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        if (events.isEmpty()) {
            item {
                EmptyState()
            }
        } else {
            upcomingEvents.forEach { (_, eventsForDay) ->
                item {
                    val date = eventsForDay.first().date
                    Text(
                        text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                            .format(Date(date)),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(eventsForDay) { event ->
                    EventItem(event = event, onEditClick = onEditClick)
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.AddCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No events scheduled",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventItem(
    event: Event,
    onEditClick: (Event) -> Unit
) {
    val eventDate = Instant.ofEpochMilli(event.date).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    val isToday = eventDate == today
    val isPast = eventDate.isBefore(today)
    val isFuture = eventDate.isAfter(today)
    val isCurrentWeek = eventDate.isAfter(today.minusDays(1)) && eventDate.isBefore(today.plusDays(7))

    // Color definitions
    val containerColor = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer
        isPast -> MaterialTheme.colorScheme.surfaceVariant
        isFuture && isCurrentWeek -> MaterialTheme.colorScheme.secondaryContainer
        isFuture -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val statusText = when {
        isToday -> "Today"
        isPast -> "Past"
        isFuture && isCurrentWeek -> "This Week"
        isFuture -> "Upcoming"
        else -> ""
    }

    val statusColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        isFuture && isCurrentWeek -> MaterialTheme.colorScheme.onSecondaryContainer
        isFuture -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val timeColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onEditClick(event) },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (isToday) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title and Time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                        isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = timeColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Status and Date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (statusText.isNotEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                color = statusColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Text(
                    text = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(event.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = timeColor
                )
            }

            // Description
            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

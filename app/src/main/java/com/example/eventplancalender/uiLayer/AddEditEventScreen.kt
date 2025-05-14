package com.example.eventplancalender.uiLayer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventplancalender.entity.Event
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

// 6. Enhanced Add/Edit Screen with Time Picker
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    event: Event?,
    onSave: (Event) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(event?.title ?: "") }
    var description by remember { mutableStateOf(event?.description ?: "") }
    var time by remember { mutableStateOf(event?.time ?: "") }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    // Initialize with event date or current time
    var dateMillis by remember { mutableStateOf(
        event?.date ?: calendar.timeInMillis
    )}

    // Time picker dialog state
    val showTimePicker = remember { mutableStateOf(false) }

    // Date picker dialog state
    val showDatePicker = remember { mutableStateOf(false) }

    // Update the calendar whenever dateMillis changes
    val currentCalendar by remember(dateMillis) {
        derivedStateOf {
            Calendar.getInstance().apply { timeInMillis = dateMillis }
        }
    }

    // Show time picker dialog when requested
    if (showTimePicker.value) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                // Update the dateMillis with the new time
                dateMillis = currentCalendar.apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }.timeInMillis
                showTimePicker.value = false
            },
            currentCalendar.get(Calendar.HOUR_OF_DAY),
            currentCalendar.get(Calendar.MINUTE),
            false
        ).apply {
            setOnDismissListener { showTimePicker.value = false }
            show()
        }
    }

    // Show date picker dialog when requested
    if (showDatePicker.value) {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                // Preserve the existing time when changing the date
                dateMillis = currentCalendar.apply {
                    set(year, month, day)
                }.timeInMillis
                showDatePicker.value = false
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { showDatePicker.value = false }
            show()
        }
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (event == null) "New Event" else "Edit Event") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (event != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                isError = title.isBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Date & Time",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Date selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault())
                        .format(Date(dateMillis)),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { showDatePicker.value = true }
                ) {
                    Text("Change Date")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = time.ifEmpty {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(dateMillis))
                    },
                    onValueChange = {},
                    label = { Text("Time *") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    isError = time.isBlank(),
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker.value = true }) {
                            Icon(Icons.Default.DateRange, "Select Time")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && time.isNotBlank()) {
                        onSave(
                            Event(
                                id = event?.id ?: 0,
                                title = title,
                                description = description,
                                date = dateMillis,
                                time = time
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && time.isNotBlank()
            ) {
                Text("Save Event")
            }
        }
    }
}
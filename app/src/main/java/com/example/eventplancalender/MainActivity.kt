package com.example.eventplancalender

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eventplancalender.dataLayer.database.EventDatabase
import com.example.eventplancalender.dataLayer.dio.EventDao
import com.example.eventplancalender.dataLayer.repository.EventRepository
import com.example.eventplancalender.entity.Event
import com.example.eventplancalender.ui.theme.EventPlanCalenderTheme
import com.example.eventplancalender.uiLayer.AddEditEventScreen
import com.example.eventplancalender.uiLayer.MainScreen

import com.example.eventplancalender.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val database = remember { EventDatabase.getDatabase(applicationContext) }
            val repository = remember { EventRepository(database.eventDao()) }
            val viewModel = ViewModelProvider(
                this,
                EventViewModelFactory(repository)
            ).get(EventViewModel::class.java)




            var currentScreen by remember { mutableStateOf("main") }
            var selectedEvent by remember { mutableStateOf<Event?>(null) }

            EventPlanCalenderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "main" -> MainScreen(
                            viewModel = viewModel,
                            onAddClick = {
                                selectedEvent = null
                                currentScreen = "add"
                            },
                            onEditClick = {
                                selectedEvent = it
                                currentScreen = "add"
                            }
                        )

                        "add" -> AddEditEventScreen(
                            event = selectedEvent,
                            onSave = {
                                if (selectedEvent == null) viewModel.addEvent(it.title, it.description, it.date)
                                else viewModel.updateEvent(it)
                                currentScreen = "main"
                            },
                            onDelete = {
                                selectedEvent?.let { viewModel.deleteEvent(it) }
                                currentScreen = "main"
                            },
                            onBack = {
                                currentScreen = "main"
                            }
                        )

                    }
                }
            }
        }
    }

    class EventViewModelFactory(
        private val repository: EventRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EventViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

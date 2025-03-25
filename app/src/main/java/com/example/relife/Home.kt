package com.example.relife

import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.relife.ViewModels.DataSharingVM
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*

@Composable
fun Home(dataSharingVM: DataSharingVM, navController: NavController) {
    val context = LocalContext.current
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val user = dataSharingVM.userLD.value
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var userLocationName by remember { mutableStateOf("Retrieving location...") }
    val locationPermissionGranted = remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted.value = isGranted
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            locationPermissionGranted.value = true
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                userLocationName = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown location"
            } catch (e: Exception) {
                userLocationName = "Failed to retrieve location"
            }
        }
    }

    var tasks by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }

    // Show the dialog to add tasks
    if (showDialog) {
        AddTaskDialog(
            onAddTask = { time, task ->
                tasks = tasks + Pair(time, task)
                showDialog = false
            },
            onDismiss = { showDialog = false },
            selectedTime = selectedTime,
            onTimeChange = { selectedTime = it },
            taskDescription = taskDescription,
            onTaskDescriptionChange = { taskDescription = it },
            onDialogOpen = {
                // Reset the values when the dialog opens
                selectedTime = ""
                taskDescription = ""
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RL",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Welcome",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = user?.name ?: "Stranger",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Location Section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "You are currently in:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userLocationName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Placeholder for Future Features (2 empty sections)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Person Details Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                        .clickable { navController.navigate(Screens.Person.screen) },
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Person Details",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Person Details",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Location Details Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp)
                        .clickable { navController.navigate(Screens.LocationInfo.screen) },
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Details",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Location Details",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }



        Spacer(modifier = Modifier.height(16.dp))

        // Task Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    IconButton(onClick = {
                        // Reset the values before opening the dialog
                        selectedTime = ""
                        taskDescription = ""
                        showDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                LazyColumn {
                    items(tasks.size) { index ->
                        TaskItem(task = tasks[index].first, time = tasks[index].second)
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onAddTask: (String, String) -> Unit,
    onDismiss: () -> Unit,
    selectedTime: String,
    onTimeChange: (String) -> Unit,
    taskDescription: String,
    onTaskDescriptionChange: (String) -> Unit,
    onDialogOpen: () -> Unit
) {
    // Reset values when dialog is opened
    LaunchedEffect(key1 = Unit) {
        onDialogOpen()
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task") },
        text = {
            Column {
                // Time picker button
                Button(
                    onClick = {
                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                // Format the selected time and update the field
                                val ampm = if (hourOfDay >= 12) "PM" else "AM"
                                val hourFormatted = if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                                val timeString = String.format("%02d:%02d %s", hourFormatted, minute, ampm)
                                onTimeChange(timeString)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        )
                        timePickerDialog.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Time")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display selected time
                Text(
                    text = "Selected Time: $selectedTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Task description input
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = onTaskDescriptionChange,
                    label = { Text("Task") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (taskDescription.isNotBlank() && selectedTime.isNotBlank()) {
                    onAddTask(selectedTime, taskDescription)
                }
            }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TaskItem(task: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = task,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

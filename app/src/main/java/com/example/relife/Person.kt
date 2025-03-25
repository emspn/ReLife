package com.example.relife

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.rememberAsyncImagePainter
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.accompanist.pager.*



data class PersonD(
    val imageUris: List<String>, // Changed to a list of image URIs
    val name: String,
    val relation: String,
    val description: String,
    val importance: Int
)






@Composable
fun Person() {
    val persons = remember { mutableStateListOf<PersonD>() }
    val showAddPersonDialog = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Person Details",
                    fontSize = 22.sp,
                    color = Color.White
                )
                IconButton(onClick = { showAddPersonDialog.value = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Person", tint = Color.White)
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(persons) { person ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    PersonRow(person = person, onDelete = {
                        persons.remove(person)
                    })
                }
            }
        }
    }

    if (showAddPersonDialog.value) {
        AddPersonDialog(onAddPerson = { person ->
            persons.add(person)
            showAddPersonDialog.value = false
        }, onDismiss = {
            showAddPersonDialog.value = false
        })
    }
}


@Composable
fun PersonRow(person: PersonD, onDelete: () -> Unit) {
    var showMore by remember { mutableStateOf(false) }
    var showImageViewer by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) } // State for delete confirmation dialog
    var selectedImageIndex by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Randomly select an image for the profile picture
        val profileImageUri = if (person.imageUris.isNotEmpty()) {
            person.imageUris.random()
        } else {
            "default"
        }

        if (profileImageUri != "default") {
            Image(
                painter = rememberAsyncImagePainter(profileImageUri),
                contentDescription = "Person Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable {
                        showImageViewer = true
                    }
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile Icon",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = person.name, fontSize = 20.sp)
            Text(text = person.relation, fontSize = 16.sp, color = Color.Gray)
            if (showMore) {
                Text(text = person.description, fontSize = 16.sp)
                Text(text = "Importance: ${person.importance}", fontSize = 16.sp)
            }
        }

        TextButton(onClick = { showMore = !showMore }) {
            Text(text = if (showMore) "Show Less" else "Show More", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }

        // Delete Button
        IconButton(onClick = { showDeleteDialog = true }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Person", tint = Color.Red)
        }
    }

    // Full-screen image viewer
    if (showImageViewer) {
        ImageViewer(images = person.imageUris, onDismiss = { showImageViewer = false })
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Person") },
            text = { Text("Do you want to delete this person's info?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false // Close the dialog after deletion
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}


@OptIn( ExperimentalPagerApi::class)
@Composable
fun ImageViewer(images: List<String>, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HorizontalPager(
                    count = images.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Image(
                        painter = rememberAsyncImagePainter(images[page]),
                        contentDescription = "Full Screen Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp)) // Rounded corners
                    )
                }

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun AddPersonDialog(onAddPerson: (PersonD) -> Unit, onDismiss: () -> Unit) {
    var imageUris by remember { mutableStateOf(mutableListOf<Uri>()) }
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var importance by remember { mutableIntStateOf(1) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        imageUris.addAll(uris)
        Log.d("Image Uris", imageUris.toString())
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Image Picker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { launcher.launch("image/*") }) {
                        Text(text = "Choose Images")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (imageUris.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUris.random()), // Randomly show one of the selected images
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Profile Icon",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("Relation") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Importance", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Slider(
                    value = importance.toFloat(),
                    onValueChange = { importance = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 4
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val person = PersonD(
                        imageUris = imageUris.map { it.toString() }, // Convert URIs to Strings
                        name = name,
                        relation = relation,
                        description = description,
                        importance = importance
                    )
                    onAddPerson(person)
                }) {
                    Text(text = "Add Person")
                }
            }
        }
    }
}



package com.example.relife

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.pager.*
import com.google.android.gms.maps.model.LatLng
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Row as Row


data class LocationInfo(
    val address: String,
    val images: List<String>,
    val remark: String,
    val latitude: Double,
    val longitude: Double,
    val placeName: String
)


@Composable
fun LocationInfoScreen(navController: NavController) {
    var locations by remember { mutableStateOf(listOf<LocationInfo>()) }
    val showAddLocationDialog = remember { mutableStateOf(false) }
    val selectedAddress = navController.currentBackStackEntry?.savedStateHandle?.get<String>("selected_address")
    val selectedLatLng = navController.currentBackStackEntry?.savedStateHandle?.get<LatLng>("selected_latLng")

    if (selectedAddress != null && selectedLatLng != null) {
        showAddLocationDialog.value = true
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.primary) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Location Details", fontSize = 22.sp, color = Color.White)
                    IconButton(onClick = { showAddLocationDialog.value = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Location", tint = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            items(locations) { location ->
                LocationDetails(location = location, onDelete = {
                    locations = locations.filter { it != location }
                }, onEdit = { updatedLocation ->
                    locations = locations.map {
                        if (it == location) updatedLocation else it
                    }
                })
            }
        }

        if (showAddLocationDialog.value) {
            AddLocationDialog(navController = navController, onAddLocation = { newLocation ->
                locations = locations + newLocation
                showAddLocationDialog.value = false
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_address")
                navController.currentBackStackEntry?.savedStateHandle?.remove<LatLng>("selected_latLng")
            }, onDismiss = {
                showAddLocationDialog.value = false
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_address")
                navController.currentBackStackEntry?.savedStateHandle?.remove<LatLng>("selected_latLng")
            })
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LocationDetails(location: LocationInfo, onDelete: () -> Unit, onEdit: (LocationInfo) -> Unit) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showImageFullScreen by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var showEditLocationDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = location.placeName, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { showEditLocationDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Edit Location") }
                IconButton(onClick = { showDeleteConfirmation = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete Location") }
            }

            LazyRow(modifier = Modifier.padding(top = 8.dp)) {
                itemsIndexed(location.images) { index, imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Location Image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                selectedImageIndex = index
                                showImageFullScreen = true
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Text(text = location.remark, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
            if (location.remark.length > 20) {
                TextButton(onClick = { /*TODO: Show Full Remark*/ }) { Text("Show More") }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Confirmation") },
            text = { Text("Are you sure you want to delete this location details?") },
            confirmButton = {
                Button(onClick = {
                    onDelete()
                    showDeleteConfirmation = false
                }) { Text("Yes") }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmation = false }) { Text("No") }
            }
        )
    }

    if (showImageFullScreen) {
        Dialog(onDismissRequest = { showImageFullScreen = false }) {
            val pagerState = rememberPagerState(initialPage = selectedImageIndex)
            HorizontalPager(
                count = location.images.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Image(
                    painter = rememberAsyncImagePainter(model = location.images[page]),
                    contentDescription = "Full Screen Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    if (showEditLocationDialog) {
        EditLocationDialog(
            location = location,
            onEditLocation = { updatedLocation ->
                onEdit(updatedLocation)
                showEditLocationDialog = false
            },
            onDismiss = { showEditLocationDialog = false }
        )
    }
}

@Composable
fun EditLocationDialog(
    location: LocationInfo,
    onEditLocation: (LocationInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var images by remember { mutableStateOf(location.images.map { Uri.parse(it) }) }
    var remark by remember { mutableStateOf(location.remark) }
    var placeName by remember { mutableStateOf(location.placeName) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        images = uris
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    label = { Text("Name of the Place") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { launcher.launch("image/*") }) {
                    Text(text = "Choose Images")
                }
                LazyRow {
                    items(images) { uri ->
                        Box(modifier = Modifier.padding(4.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { images = images.filter { it != uri } },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = Color.Red)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("Remark") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (placeName.isNotEmpty()) {
                            val updatedLocation = location.copy(
                                placeName = placeName,
                                images = images.map { it.toString() },
                                remark = remark
                            )
                            onEditLocation(updatedLocation)
                        } else {
                            Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(text = "Submit")
                }
            }
        }
    }
}

@Composable
fun AddLocationDialog(
    navController: NavController,
    onAddLocation: (LocationInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedLatLng = navController.currentBackStackEntry?.savedStateHandle?.get<LatLng>("selected_latLng")
    val selectedAddress = navController.currentBackStackEntry?.savedStateHandle?.get<String>("selected_address") ?: ""
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var remark by remember { mutableStateOf("") }
    var placeName by remember { mutableStateOf("") }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        images = uris
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Button(onClick = { navController.navigate("map_picker") }) {
                    Text(text = "Choose Address from Map")
                }

                if (selectedAddress.isNotEmpty()) {
                    Text(text = selectedAddress, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                selectedLatLng?.let {
                    Text(text = "Latitude: ${it.latitude}", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp))
                    Text(text = "Longitude: ${it.longitude}", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    label = { Text("Name of the Place") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { launcher.launch("image/*") }) {
                    Text(text = "Choose Images")
                }
                LazyRow {
                    items(images) { uri ->
                        Box(modifier = Modifier.padding(4.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { images = images.filter { it != uri } },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = Color.Red)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("Remark") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (selectedAddress.isNotEmpty() && selectedLatLng != null && placeName.isNotEmpty()) {
                            val imageUris = images.map { it.toString() }
                            val location = LocationInfo(
                                address = selectedAddress,
                                images = imageUris,
                                remark = remark,
                                latitude = selectedLatLng.latitude,
                                longitude = selectedLatLng.longitude,
                                placeName = placeName
                            )
                            onAddLocation(location)
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(text = "Submit")
                }
            }
        }
    }
}



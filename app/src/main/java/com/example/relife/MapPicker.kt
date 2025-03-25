package com.example.relife
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(navController: NavController, onAddressSelected: (String, LatLng) -> Unit) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val locationPermissionGranted = remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf("") }

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
            if (locationPermissionGranted.value) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val userLocation = LatLng(it.latitude, it.longitude)
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map Picker") },
                actions = {
                    if (selectedLocation != null) {
                        IconButton(onClick = {
                            onAddressSelected(selectedAddress, selectedLocation!!)
                            navController.previousBackStackEntry?.savedStateHandle?.set("selected_address", selectedAddress)
                            navController.previousBackStackEntry?.savedStateHandle?.set("selected_latLng", selectedLocation)
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Submit")
                        }
                    }
                }
            )
        },
        content = { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = locationPermissionGranted.value),
                    onMapClick = { latLng ->
                        selectedLocation = latLng
                        getAddressFromLatLng(context, latLng) { address ->
                            selectedAddress = address
                        }
                    }
                ) {
                    selectedLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Selected Location"
                        )
                    }
                }
            }
        }
    )
}

fun getAddressFromLatLng(context: Context, latLng: LatLng, onAddressFetched: (String) -> Unit) {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0].getAddressLine(0) ?: "Address Not Found"
            onAddressFetched(address)
        } else {
            onAddressFetched("Address Not Found")
        }
    } catch (e: Exception) {
        onAddressFetched("Geocoding failed: ${e.message}")
    }
}

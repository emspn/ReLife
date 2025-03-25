package com.example.relife
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.relife.ViewModels.DataSharingVM
import com.example.relife.ui.theme.ReLifeTheme
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dataSharingVM: DataSharingVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        installSplashScreen()
        enableEdgeToEdge()
        dataSharingVM = ViewModelProvider(this)[DataSharingVM::class.java]
        auth = Firebase.auth

        setContent {
            ReLifeTheme {
                ProvideWindowInsets {
                    MyApp(auth, dataSharingVM)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyApp(auth: FirebaseAuth, dataSharingVM: DataSharingVM) {
    val navigationController = rememberNavController()
    val selected = remember { mutableStateOf(Icons.Default.Home) }
    var isLoggedIn by remember { mutableStateOf(auth.currentUser  != null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Adding AuthStateListener
    DisposableEffect(auth) {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            Log.d("AuthStateListener", "AuthState changed: ${user?.email}")
            isLoggedIn = user != null
        }

        if (isLoggedIn) {
            Firebase.database.getReference("users")
                .child(Firebase.auth.currentUser !!.uid)
                .get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(com.example.relife.Model.User::class.java)
                        if (user != null) {
                            Log.d("User  data", user.toString())
                            dataSharingVM.setUser (user)
                        } else {
                            Log.d("User  data", "User  is null")
                        }
                    } else {
                        Log.d("User  data", "Snapshot does not exist")
                    }
                }
                .addOnFailureListener {
                    Log.d("User  Data", it.message.toString())
                }
        }

        auth.addAuthStateListener(authStateListener)

        onDispose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    Log.d("MyAppComposable", "isLoggedIn: $isLoggedIn")

    if (!isLoggedIn) {
        NavHost(
            navController = navigationController,
            startDestination = Screens.SignIn.screen
        ) {
            composable(Screens.SignIn.screen) {
                SignIn(modifier = Modifier.fillMaxSize(), onLoginSuccess = {
                    Log.d("MyAppComposable", "Navigating to Home screen")
                    isLoggedIn = true // Update login state on success
                    navigationController.navigate(Screens.Home.screen) {
                        popUpTo(Screens.SignIn.screen) { inclusive = true }
                    }
                })
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                    IconButton(
                        onClick = {
                            selected.value = Icons.Default.Home
                            navigationController.navigate(Screens.Home.screen) { popUpTo(0) }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Icons.Default.Home) Color.LightGray else Color.DarkGray
                        )
                    }
                    IconButton(
                        onClick = {
                            selected.value = Icons.Default.LocationOn
                            navigationController.navigate(Screens.Map.screen) { popUpTo(0) }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Icons.Default.LocationOn) Color.LightGray else Color.DarkGray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(onClick = { showBottomSheet = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.DarkGray
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            selected.value = Icons.Default.Send
                            navigationController.navigate(Screens.Chat.screen) { popUpTo(0) }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Icons.Default.Send) Color.LightGray else Color.DarkGray
                        )
                    }
                    IconButton(
                        onClick = {
                            selected.value = Icons.Default.AccountCircle
                            navigationController.navigate(Screens.Profile.screen) { popUpTo(0) }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Icons.Default.AccountCircle) Color.LightGray else Color.DarkGray
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navigationController,
                startDestination = Screens.Home.screen,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screens.Home.screen) { Home(dataSharingVM, navigationController) }
                composable(Screens.Map.screen) { Map() }
                composable(Screens.Chat.screen) { Chat() }
                composable(Screens.Profile.screen) { Profile() }
                composable(Screens.Person.screen) { Person() } // Pass db here
                composable(Screens.LocationInfo.screen) {
                    LocationInfoScreen(
                        navigationController
                    )
                }
                composable("map_picker") {
                    MapPickerScreen(
                        navController = navigationController,
                        onAddressSelected = { address, latLng ->
                            navigationController.previousBackStackEntry?.savedStateHandle?.set(
                                "selected_address",
                                address
                            )
                            navigationController.previousBackStackEntry?.savedStateHandle?.set(
                                "selected_latLng",
                                latLng
                            )
                        })
                }
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    BottomSheetItem(
                        icon = Icons.Default.AccountCircle,
                        title = "Person Details"
                    ) {
                        showBottomSheet = false
                        navigationController.navigate(Screens.Person.screen) { popUpTo(0) }
                    }
                    BottomSheetItem(
                        icon = Icons.Default.LocationOn,
                        title = "Location Details"
                    ) {
                        showBottomSheet = false
                        navigationController.navigate(Screens.LocationInfo.screen) { popUpTo(0) }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSheetItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}





fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "task_channel"
        val channelName = "Task Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.description = "This channel is used for task reminders."

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

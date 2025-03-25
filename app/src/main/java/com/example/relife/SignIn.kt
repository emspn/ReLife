package com.example.relife

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import com.example.relife.Model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@Composable
fun SignIn(modifier: Modifier = Modifier, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        scope.launch {
            Log.d("SignInComposable", "Sign-In result received")
            GoogleSignInUtils.handleSignInResult(
                task = GoogleSignIn.getSignedInAccountFromIntent(result.data),
                login = {
                    Log.d("SignInComposable", "Login successful")
                    onLoginSuccess() // Update login state on success
                },
                onError = { exception ->
                    Log.e("SignInComposable", "Login failed: ${exception.message}")
                }
            )
        }
    }

    // Setup Google Sign-In
    GoogleSignInUtils.setupGoogleSignIn(context)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var name by remember { mutableStateOf(TextFieldValue("")) }
        var email by remember { mutableStateOf(TextFieldValue("")) }
        var password by remember { mutableStateOf(TextFieldValue("")) }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = "Full Name") }
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") }
        )

        Button(onClick = {
            Log.d("SignInComposable", "Google Sign-In button clicked")
//            GoogleSignInUtils.doGoogleSignIn(launcher)
            if (email.text.isNotEmpty() && password.text.isNotEmpty()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.text, password.text)
                    .addOnSuccessListener {
                        Log.d("Signup", "Success ${it.user!!.uid}")
                        val user = User(
                            userId = it.user!!.uid,
                            name = name.text
                        )
                        storeUser(user)
                    }
                    .addOnFailureListener {
                        Log.e("Signup", "Failed")
                    }
            }
        }) {
            Text(text = "Google Sign in")
        }
    }
}

private fun storeUser(user: User) {
    FirebaseDatabase.getInstance().getReference("Users").child(user.userId)
        .setValue(user).addOnSuccessListener {
            Log.d("Firebase Database", "Success ${it}")
        }
        .addOnFailureListener {
            Log.d("Firebase Database", "Error: ${it.message}")
        }
}




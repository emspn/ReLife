package com.example.relife

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class GoogleSignInUtils {

    companion object {
        private const val TAG = "GoogleSignInUtils"
        private lateinit var googleSignInClient: GoogleSignInClient

        fun setupGoogleSignIn(context: Context) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.web_client_id))
                .requestEmail()
                .build()
            this.googleSignInClient = GoogleSignIn.getClient(context, gso)
            Log.d(TAG, "Google Sign-In client set up")
        }

        fun doGoogleSignIn(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
            Log.d("doGoogleSign", "Initiating Google Sign-In")
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }

        suspend fun handleSignInResult(task: Task<GoogleSignInAccount>, login: () -> Unit, onError: (Exception) -> Unit) {
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "Google Sign-In account retrieved: ${account?.email}")
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    val authResult = Firebase.auth.signInWithCredential(credential).await()
                    if (authResult.user != null) {
                        Log.d(TAG, "Firebase Auth success: ${authResult.user?.email}")
                        login.invoke()
                    } else {
                        Log.e(TAG, "Firebase Auth failed: Auth result user is null")
                        onError(Exception("Firebase Auth failed: Auth result user is null"))
                    }
                } else {
                    Log.e(TAG, "Google Sign-In failed: Account is null")
                    onError(Exception("Google Sign-In failed: Account is null"))
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google Sign-In failed: ${e.statusCode}")
                onError(e)
            }
        }

    }
}

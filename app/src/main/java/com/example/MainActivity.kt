package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BillsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BillViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(FirebaseAuth.getInstance().currentUser) }
    var isGuestMode by remember { mutableStateOf(prefs.getBoolean("guest_mode", false)) }

    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                isGuestMode = false
                prefs.edit().putBoolean("guest_mode", false).apply()
            }
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null && !isGuestMode) {
        MyApplicationTheme(darkTheme = false) {
            SignInScreen(
                onAuthSuccess = {
                    currentUser = FirebaseAuth.getInstance().currentUser
                    isGuestMode = false
                    prefs.edit().putBoolean("guest_mode", false).apply()
                },
                onContinueAsGuest = {
                    isGuestMode = true
                    prefs.edit().putBoolean("guest_mode", true).apply()
                }
            )
        }
    } else {
        val viewModel: BillViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        MyApplicationTheme(darkTheme = uiState.isDarkMode) {
            BillsScreen(
                viewModel = viewModel,
                onSignOutRequested = {
                    isGuestMode = false
                    prefs.edit().putBoolean("guest_mode", false).apply()
                    viewModel.signOut()
                }
            )
        }
    }
}

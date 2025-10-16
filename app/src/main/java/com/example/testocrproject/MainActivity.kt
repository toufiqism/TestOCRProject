package com.example.testocrproject

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.testocrproject.ui.theme.TestOCRProjectTheme

/**
 * Main Activity for the OCR Application
 * Handles navigation between Camera Capture Screen and Settings Screen
 * Manages theme changes based on user preferences
 */
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("Permission", "Camera permission granted.")
                // Recomposition will be triggered by the state change in the composable
            } else {
                Log.d("Permission", "Camera permission denied.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OCRApp()
        }
    }
}

/**
 * Main App Composable with navigation and theme management
 */
@Composable
fun OCRApp() {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // State to trigger recomposition when theme changes
    var themeVersion by remember { mutableStateOf(0) }
    var currentScreen by remember { mutableStateOf(Screen.Camera) }
    
    // Determine the dark theme based on user preference
    val themeMode = remember(themeVersion) { preferencesManager.getThemeMode() }
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDarkTheme
    }
    
    TestOCRProjectTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                Screen.Camera -> {
                    CameraCaptureScreen(
                        onNavigateToSettings = { currentScreen = Screen.Settings }
                    )
                }
                Screen.Settings -> {
                    SettingsScreen(
                        onNavigateBack = { currentScreen = Screen.Camera },
                        onThemeChanged = { themeVersion++ }
                    )
                }
            }
        }
    }
}

/**
 * Enum for screen navigation
 */
enum class Screen {
    Camera,
    Settings
}

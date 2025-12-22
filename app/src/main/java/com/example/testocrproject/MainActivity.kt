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
 * Optimized for minimal recomposition using lambda-based state reading
 */
@Composable
fun OCRApp() {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    
    // State to trigger recomposition when theme changes
    var themeVersion by remember { mutableIntStateOf(0) }
    var currentScreen by remember { mutableStateOf(Screen.Camera) }
    
    // Theme wrapper that reads state as late as possible
    ThemedContent(
        preferencesManager = preferencesManager,
        themeVersion = themeVersion
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Use key to prevent unnecessary recomposition when switching screens
            key(currentScreen) {
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
}

/**
 * Wrapper composable that handles theme determination
 * Reads theme state as late as possible to minimize recomposition scope
 */
@Composable
private fun ThemedContent(
    preferencesManager: PreferencesManager,
    themeVersion: Int,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // Read theme mode only when themeVersion changes
    val darkTheme = remember(themeVersion, systemInDarkTheme) {
        when (preferencesManager.getThemeMode()) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> systemInDarkTheme
        }
    }
    
    TestOCRProjectTheme(darkTheme = darkTheme, content = content)
}

/**
 * Enum for screen navigation
 */
enum class Screen {
    Camera,
    Settings
}

package com.example.testocrproject

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier

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
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                CameraCaptureScreen()
//                BanglaOcrApp(
//                    requestPermission = {
//                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//                    }
//                )
            }
        }
    }
}

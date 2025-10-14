package com.example.testocrproject

import CameraCaptureScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun CameraOCRScreen(viewModel: OCRViewModel) {
    var capturedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (capturedBitmap == null) {
            CameraCaptureScreen { bitmap ->
                capturedBitmap = bitmap
                viewModel.processImage(bitmap)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Image(
                    bitmap = capturedBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                Text(
                    text = viewModel.recognizedText.value,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )

                Button(onClick = { capturedBitmap = null }) {
                    Text("Capture Again")
                }
            }
        }
    }
}

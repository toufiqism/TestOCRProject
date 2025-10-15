package com.example.testocrproject

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraCaptureScreen(viewModel: OCRViewModel = viewModel()) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    val hasImage by remember { derivedStateOf { imageUri != null } }
    val uiState by viewModel.uiState.collectAsState()

    fun getUriFromFile(): Pair<Uri, File> {
        val file = context.createImageFile()
        val uri = FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            context.packageName + ".provider",
            file
        )
        return uri to file
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Toast.makeText(context, "Image Captured!", Toast.LENGTH_SHORT).show()
                imageFile?.let {
                    viewModel.uploadImage(it)
                }
            } else {
                imageUri = null
                imageFile = null
                Toast.makeText(context, "Capture Cancelled.", Toast.LENGTH_SHORT).show()
            }
        }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it // Set URI for image preview
            // Create a file from the content URI to upload
            val file = context.createFileFromUri(it)
            imageFile = file
            viewModel.uploadImage(file)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            val (uri, file) = getUriFromFile()
            imageUri = uri
            imageFile = file
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasImage && imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
        } else {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Capture or select an image to start.")
            }
        }

        // Display UI based on upload state
        when (val state = uiState) {
            is UploadState.Idle -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        val permissionCheckResult =
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                            val (uri, file) = getUriFromFile()
                            imageUri = uri
                            imageFile = file
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Text(text = "Take Picture")
                    }
                    Button(onClick = {
                        galleryLauncher.launch("image/*")
                    }) {
                        Text(text = "Select Image")
                    }
                }
            }

            is UploadState.Loading -> {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Uploading...")
                }
            }

            is UploadState.Success -> {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Extracted Text: ${state.extractedText ?: "No text found."}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        viewModel.resetState()
                        imageUri = null
                        imageFile = null
                    }) {
                        Text("Start Over")
                    }
                }

            }

            is UploadState.Error -> {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        viewModel.resetState()
                        imageUri = null
                        imageFile = null
                    }) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = externalCacheDir
    return File.createTempFile(imageFileName, ".jpg", storageDir)
}

/**
 * Creates a temporary file from a content URI.
 * This is useful for handling images selected from the gallery.
 */
fun Context.createFileFromUri(uri: Uri): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "JPEG_${timeStamp}"
    val tempFile = File.createTempFile(fileName, ".jpg", cacheDir)

    // Copy the content from the URI's input stream to the temporary file
    contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return tempFile
}
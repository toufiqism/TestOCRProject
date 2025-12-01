package com.example.testocrproject

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: OCRViewModel = viewModel()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    val hasImage by remember { derivedStateOf { imageUri != null } }
    val uiState by viewModel.uiState.collectAsState()
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val permissionHandler = remember { PermissionHandler(context) }
    
    // Permission dialogs state
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var showStoragePermissionDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var deniedPermissionMessage by remember { mutableStateOf("") }

    // FIX: Use derivedStateOf to always get current base URL (fixes memory leak)
    val currentBaseUrl by remember { derivedStateOf { preferencesManager.getBaseUrl() } }

    // Cleanup function for temporary files
    fun cleanupTempFile(file: File?) {
        file?.let {
            try {
                if (it.exists()) {
                    it.delete()
                }
            } catch (e: Exception) {
                // Silently handle cleanup errors
                Log.e("ERROR", e.stackTraceToString())
            }
        }
    }

    // Cleanup on disposal
    DisposableEffect(Unit) {
        onDispose {
            cleanupTempFile(imageFile)
        }
    }

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
                imageFile?.let { file ->
                    // Check network before upload
                    if (context.isNetworkAvailable()) {
                        viewModel.uploadImage(file, currentBaseUrl)
                    } else {
                        Toast.makeText(
                            context,
                            "No internet connection. Please check your network.",
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.setError("No internet connection")
                    }
                }
            } else {
                // Clean up file if capture was cancelled
                cleanupTempFile(imageFile)
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
            try {
                val file = context.createFileFromUri(it)
                imageFile = file

                // Check network before upload
                if (context.isNetworkAvailable()) {
                    viewModel.uploadImage(file, currentBaseUrl)
                } else {
                    Toast.makeText(
                        context,
                        "No internet connection. Please check your network.",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.setError("No internet connection")
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to load image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            val (uri, file) = getUriFromFile()
            imageUri = uri
            imageFile = file
            cameraLauncher.launch(uri)
        } else {
            // Permission denied
            deniedPermissionMessage = "Camera permission is required to take photos. Please grant permission in app settings."
            showPermissionDeniedDialog = true
        }
    }
    
    // Storage permission launcher
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(context, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            galleryLauncher.launch("image/*")
        } else {
            // Permission denied
            deniedPermissionMessage = "Storage permission is required to select images from gallery. Please grant permission in app settings."
            showPermissionDeniedDialog = true
        }
    }
    
    // Function to handle camera button click
    fun handleCameraClick() {
        when {
            permissionHandler.isCameraPermissionGranted() -> {
                // Permission already granted, proceed with camera
                val (uri, file) = getUriFromFile()
                imageUri = uri
                imageFile = file
                cameraLauncher.launch(uri)
            }
            else -> {
                // Request camera permission
                showCameraPermissionDialog = true
            }
        }
    }
    
    // Function to handle gallery button click
    fun handleGalleryClick() {
        when {
            permissionHandler.isStoragePermissionGranted() -> {
                // Permission already granted, proceed with gallery
                galleryLauncher.launch("image/*")
            }
            else -> {
                // Request storage permission
                showStoragePermissionDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OCR Image Extractor") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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
                        .padding(bottom = 32.dp, top = 32.dp)
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
                        Button(onClick = { handleCameraClick() }) {
                            Text(text = "Take Picture")
                        }
                        Button(onClick = { handleGalleryClick() }) {
                            Text(text = "Select Image")
                        }
                    }
                }

                is UploadState.Loading -> {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Uploading...")
                    }
                }

                is UploadState.Success -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Extracted Text: ${state.extractedText ?: "No text found."}")
                        Button(onClick = {
                            // Clean up previous file before resetting
                            cleanupTempFile(imageFile)
                            viewModel.resetState()
                            imageUri = null
                            imageFile = null
                        }) {
                            Text("Start Over")
                        }
                    }

                }

                is UploadState.Error -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            // Clean up previous file before resetting
                            cleanupTempFile(imageFile)
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
        
        // Permission Dialogs
        if (showCameraPermissionDialog) {
            PermissionRationaleDialog(
                title = "Camera Permission Required",
                message = "This app needs camera permission to take photos for text extraction. Please grant camera permission to use this feature.",
                onDismiss = { showCameraPermissionDialog = false },
                onConfirm = {
                    showCameraPermissionDialog = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }
        
        if (showStoragePermissionDialog) {
            PermissionRationaleDialog(
                title = "Storage Permission Required",
                message = "This app needs storage permission to access images from your gallery. Please grant storage permission to use this feature.",
                onDismiss = { showStoragePermissionDialog = false },
                onConfirm = {
                    showStoragePermissionDialog = false
                    val permissions = PermissionHandler.getStoragePermissions()
                    storagePermissionLauncher.launch(permissions)
                }
            )
        }
        
        if (showPermissionDeniedDialog) {
            PermissionDeniedDialog(
                message = deniedPermissionMessage,
                onDismiss = { showPermissionDeniedDialog = false },
                onOpenSettings = {
                    showPermissionDeniedDialog = false
                    context.openAppSettings()
                }
            )
        }
    }
}

/**
 * Dialog to explain why permission is needed
 */
@Composable
private fun PermissionRationaleDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog shown when permission is permanently denied
 */
@Composable
private fun PermissionDeniedDialog(
    message: String,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Permission Denied") },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Extension function to open app settings
 */
fun Context.openAppSettings() {
    val intent = android.content.Intent(
        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        android.net.Uri.fromParts("package", packageName, null)
    )
    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
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
 * @throws Exception if file creation or copying fails
 */
fun Context.createFileFromUri(uri: Uri): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "JPEG_${timeStamp}"
    val tempFile = File.createTempFile(fileName, ".jpg", cacheDir)

    try {
        // Copy the content from the URI's input stream to the temporary file
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalStateException("Unable to open input stream for URI: $uri")

        return tempFile
    } catch (e: Exception) {
        // Clean up file if copy fails
        tempFile.delete()
        throw e
    }
}

/**
 * Checks if network connectivity is available
 * @return true if connected to network, false otherwise
 */
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        return networkInfo != null && networkInfo.isConnected
    }
}
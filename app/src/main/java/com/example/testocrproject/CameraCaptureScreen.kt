package com.example.testocrproject

import android.Manifest
import android.content.Context
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
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    
    // Image state - kept at this level as it's needed for multiple child composables
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    
    // Image editor state
    var showImageEditor by remember { mutableStateOf(false) }
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Permission dialogs state - kept minimal
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var showStoragePermissionDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var deniedPermissionMessage by remember { mutableStateOf("") }

    // Remember instances that don't change
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val permissionHandler = remember { PermissionHandler(context) }

    // Cleanup function for temporary files
    val cleanupTempFile: (File?) -> Unit = remember {
        { file ->
            file?.let {
                try {
                    if (it.exists()) it.delete()
                } catch (e: Exception) {
                    Log.e("ERROR", e.stackTraceToString())
                }
            }
        }
    }

    // Cleanup on disposal
    DisposableEffect(Unit) {
        onDispose { cleanupTempFile(imageFile) }
    }

    // File creation helper - stable reference
    val getUriFromFile: () -> Pair<Uri, File> = remember(context) {
        {
            val file = context.createImageFile()
            val uri = FileProvider.getUriForFile(
                Objects.requireNonNull(context),
                context.packageName + ".provider",
                file
            )
            uri to file
        }
    }

    // Upload helper - reads baseUrl late
    val uploadWithNetworkCheck: (File) -> Unit = remember(viewModel, preferencesManager, context) {
        { file ->
            if (context.isNetworkAvailable()) {
                viewModel.uploadImage(file, preferencesManager.getBaseUrl())
            } else {
                Toast.makeText(
                    context,
                    "No internet connection. Please check your network.",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.setError("No internet connection")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Toast.makeText(context, "Image Captured!", Toast.LENGTH_SHORT).show()
            // Show image editor instead of uploading directly
            imageUri?.let {
                pendingImageUri = it
                showImageEditor = true
            }
        } else {
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
            imageUri = it
            // Show image editor instead of uploading directly
            pendingImageUri = it
            showImageEditor = true
        }
    }

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
            deniedPermissionMessage = "Camera permission is required to take photos. Please grant permission in app settings."
            showPermissionDeniedDialog = true
        }
    }
    
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            Toast.makeText(context, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            galleryLauncher.launch("image/*")
        } else {
            deniedPermissionMessage = "Storage permission is required to select images from gallery. Please grant permission in app settings."
            showPermissionDeniedDialog = true
        }
    }
    
    // Stable click handlers
    val handleCameraClick: () -> Unit = remember(permissionHandler, cameraPermissionLauncher, cameraLauncher) {
        {
            if (permissionHandler.isCameraPermissionGranted()) {
                val (uri, file) = getUriFromFile()
                imageUri = uri
                imageFile = file
                cameraLauncher.launch(uri)
            } else {
                showCameraPermissionDialog = true
            }
        }
    }
    
    val handleGalleryClick: () -> Unit = remember(permissionHandler, galleryLauncher) {
        {
            if (permissionHandler.isStoragePermissionGranted()) {
                galleryLauncher.launch("image/*")
            } else {
                showStoragePermissionDialog = true
            }
        }
    }

    val handleReset: () -> Unit = remember(viewModel, cleanupTempFile) {
        {
            cleanupTempFile(imageFile)
            viewModel.resetState()
            imageUri = null
            imageFile = null
        }
    }

    // Show image editor if an image is pending
    if (showImageEditor && pendingImageUri != null) {
        ImageEditorScreen(
            imageUri = pendingImageUri!!,
            onImageEdited = { editedFile ->
                showImageEditor = false
                imageFile = editedFile
                imageUri = Uri.fromFile(editedFile)
                pendingImageUri = null
                uploadWithNetworkCheck(editedFile)
            },
            onCancel = {
                showImageEditor = false
                pendingImageUri = null
                // Clean up camera file if it was from camera
                cleanupTempFile(imageFile)
                imageUri = null
                imageFile = null
            }
        )
        return
    }

    Scaffold(
        topBar = {
            CameraCaptureTopBar(onNavigateToSettings = onNavigateToSettings)
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
            // Image preview section
            ImagePreviewSection(
                imageUri = imageUri,
                modifier = Modifier.weight(1f)
            )

            // State-dependent content - reads ViewModel state late
            UploadStateContent(
                viewModel = viewModel,
                onCameraClick = handleCameraClick,
                onGalleryClick = handleGalleryClick,
                onReset = handleReset,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Permission Dialogs - only recompose when dialog state changes
        PermissionDialogs(
            showCameraDialog = showCameraPermissionDialog,
            showStorageDialog = showStoragePermissionDialog,
            showDeniedDialog = showPermissionDeniedDialog,
            deniedMessage = deniedPermissionMessage,
            onDismissCameraDialog = { showCameraPermissionDialog = false },
            onDismissStorageDialog = { showStoragePermissionDialog = false },
            onDismissDeniedDialog = { showPermissionDeniedDialog = false },
            onConfirmCameraPermission = {
                showCameraPermissionDialog = false
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onConfirmStoragePermission = {
                showStoragePermissionDialog = false
                storagePermissionLauncher.launch(PermissionHandler.getStoragePermissions())
            },
            onOpenSettings = {
                showPermissionDeniedDialog = false
                context.openAppSettings()
            }
        )
    }
}


/**
 * Top app bar - extracted to prevent recomposition when other state changes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraCaptureTopBar(
    onNavigateToSettings: () -> Unit
) {
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

/**
 * Image preview section - only recomposes when imageUri changes
 */
@Composable
private fun ImagePreviewSection(
    imageUri: Uri?,
    modifier: Modifier = Modifier
) {
    if (imageUri != null) {
        Image(
            painter = rememberAsyncImagePainter(model = imageUri),
            contentDescription = "Selected Image",
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 32.dp)
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("Capture or select an image to start.")
        }
    }
}

/**
 * Upload state content - reads ViewModel state as late as possible
 * Each state branch is a separate composable to minimize recomposition scope
 */
@Composable
private fun UploadStateContent(
    viewModel: OCRViewModel,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect state with lifecycle awareness - state read happens here, late in the tree
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (val state = uiState) {
        is UploadState.Idle -> {
            IdleStateContent(
                onCameraClick = onCameraClick,
                onGalleryClick = onGalleryClick,
                modifier = modifier
            )
        }
        is UploadState.Loading -> {
            LoadingStateContent(modifier = modifier)
        }
        is UploadState.Success -> {
            SuccessStateContent(
                extractedText = state.extractedText,
                onReset = onReset,
                modifier = modifier
            )
        }
        is UploadState.Error -> {
            ErrorStateContent(
                errorMessage = state.message,
                onReset = onReset,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun IdleStateContent(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onCameraClick) {
            Text(text = "Take Picture")
        }
        Button(onClick = onGalleryClick) {
            Text(text = "Select Image")
        }
    }
}

@Composable
private fun LoadingStateContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Uploading...")
    }
}

@Composable
private fun SuccessStateContent(
    extractedText: String?,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Extracted Text: ${extractedText ?: "No text found."}")
        Button(onClick = onReset) {
            Text("Start Over")
        }
    }
}

@Composable
private fun ErrorStateContent(
    errorMessage: String,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onReset) {
            Text("Try Again")
        }
    }
}

/**
 * Permission dialogs container - isolated to prevent main content recomposition
 */
@Composable
private fun PermissionDialogs(
    showCameraDialog: Boolean,
    showStorageDialog: Boolean,
    showDeniedDialog: Boolean,
    deniedMessage: String,
    onDismissCameraDialog: () -> Unit,
    onDismissStorageDialog: () -> Unit,
    onDismissDeniedDialog: () -> Unit,
    onConfirmCameraPermission: () -> Unit,
    onConfirmStoragePermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    if (showCameraDialog) {
        PermissionRationaleDialog(
            title = "Camera Permission Required",
            message = "This app needs camera permission to take photos for text extraction. Please grant camera permission to use this feature.",
            onDismiss = onDismissCameraDialog,
            onConfirm = onConfirmCameraPermission
        )
    }
    
    if (showStorageDialog) {
        PermissionRationaleDialog(
            title = "Storage Permission Required",
            message = "This app needs storage permission to access images from your gallery. Please grant storage permission to use this feature.",
            onDismiss = onDismissStorageDialog,
            onConfirm = onConfirmStoragePermission
        )
    }
    
    if (showDeniedDialog) {
        PermissionDeniedDialog(
            message = deniedMessage,
            onDismiss = onDismissDeniedDialog,
            onOpenSettings = onOpenSettings
        )
    }
}

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

fun Context.createFileFromUri(uri: Uri): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "JPEG_${timeStamp}"
    val tempFile = File.createTempFile(fileName, ".jpg", cacheDir)

    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalStateException("Unable to open input stream for URI: $uri")
        return tempFile
    } catch (e: Exception) {
        tempFile.delete()
        throw e
    }
}

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

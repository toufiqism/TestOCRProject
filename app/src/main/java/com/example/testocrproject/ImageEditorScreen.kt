package com.example.testocrproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(
    imageUri: Uri,
    onImageEdited: (File) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var editedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGreyscale by remember { mutableStateOf(false) }
    var isCropping by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    
    // Crop rectangle state (normalized 0-1 values)
    var cropRect by remember { mutableStateOf(Rect(0.1f, 0.1f, 0.9f, 0.9f)) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var imageRect by remember { mutableStateOf(Rect.Zero) }

    // Load bitmap from URI
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            bitmap = context.loadBitmapFromUri(imageUri)
            editedBitmap = bitmap
        }
    }

    // Apply greyscale effect
    LaunchedEffect(isGreyscale, bitmap) {
        if (bitmap != null) {
            withContext(Dispatchers.IO) {
                editedBitmap = if (isGreyscale) {
                    bitmap!!.toGreyscale()
                } else {
                    bitmap
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Image") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isProcessing = true
                            // Apply crop and save
                            val finalBitmap = if (isCropping && editedBitmap != null) {
                                editedBitmap!!.cropToRect(cropRect)
                            } else {
                                editedBitmap
                            }
                            finalBitmap?.let { bmp ->
                                val file = context.saveBitmapToFile(bmp)
                                onImageEdited(file)
                            }
                        },
                        enabled = editedBitmap != null && !isProcessing
                    ) {
                        Icon(Icons.Default.Check, "Apply")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Image preview area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (editedBitmap != null) {
                    ImagePreviewWithCrop(
                        bitmap = editedBitmap!!,
                        isCropping = isCropping,
                        cropRect = cropRect,
                        onCropRectChange = { cropRect = it },
                        onSizeChanged = { size, imgRect ->
                            canvasSize = size
                            imageRect = imgRect
                        }
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            // Edit controls
            EditControlsBar(
                isGreyscale = isGreyscale,
                isCropping = isCropping,
                onGreyscaleToggle = { isGreyscale = !isGreyscale },
                onCropToggle = { isCropping = !isCropping },
                onReset = {
                    isGreyscale = false
                    isCropping = false
                    cropRect = Rect(0.1f, 0.1f, 0.9f, 0.9f)
                    editedBitmap = bitmap
                },
                modifier = Modifier.padding(16.dp)
            )
        }

        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}


@Composable
private fun ImagePreviewWithCrop(
    bitmap: Bitmap,
    isCropping: Boolean,
    cropRect: Rect,
    onCropRectChange: (Rect) -> Unit,
    onSizeChanged: (IntSize, Rect) -> Unit
) {
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var imageRect by remember { mutableStateOf(Rect.Zero) }
    var dragHandle by remember { mutableStateOf<CropHandle?>(null) }
    
    // Use rememberUpdatedState to get latest values in pointer input without restarting it
    val currentCropRect by rememberUpdatedState(cropRect)
    val currentImageRect by rememberUpdatedState(imageRect)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                canvasSize = size
                // Calculate image rect to fit in canvas while maintaining aspect ratio
                val canvasAspect = size.width.toFloat() / size.height
                val imageAspect = bitmap.width.toFloat() / bitmap.height
                
                val (imgWidth, imgHeight) = if (imageAspect > canvasAspect) {
                    size.width.toFloat() to size.width / imageAspect
                } else {
                    size.height * imageAspect to size.height.toFloat()
                }
                
                val left = (size.width - imgWidth) / 2
                val top = (size.height - imgHeight) / 2
                imageRect = Rect(left, top, left + imgWidth, top + imgHeight)
                onSizeChanged(size, imageRect)
            }
            .pointerInput(isCropping) {
                if (!isCropping) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        dragHandle = findCropHandle(offset, currentCropRect, currentImageRect)
                    },
                    onDrag = { change, _ ->
                        dragHandle?.let { handle ->
                            val newRect = updateCropRect(
                                handle,
                                change.position,
                                currentCropRect,
                                currentImageRect
                            )
                            onCropRectChange(newRect)
                        }
                    },
                    onDragEnd = { dragHandle = null }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw the image
            drawImage(
                image = imageBitmap,
                dstOffset = androidx.compose.ui.geometry.Offset(imageRect.left, imageRect.top)
                    .let { androidx.compose.ui.unit.IntOffset(it.x.toInt(), it.y.toInt()) },
                dstSize = androidx.compose.ui.unit.IntSize(
                    imageRect.width.toInt(),
                    imageRect.height.toInt()
                )
            )

            // Draw crop overlay if cropping
            if (isCropping) {
                val cropLeft = imageRect.left + cropRect.left * imageRect.width
                val cropTop = imageRect.top + cropRect.top * imageRect.height
                val cropRight = imageRect.left + cropRect.right * imageRect.width
                val cropBottom = imageRect.top + cropRect.bottom * imageRect.height

                // Dim areas outside crop
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(imageRect.left, imageRect.top),
                    size = Size(imageRect.width, cropTop - imageRect.top)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(imageRect.left, cropBottom),
                    size = Size(imageRect.width, imageRect.bottom - cropBottom)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(imageRect.left, cropTop),
                    size = Size(cropLeft - imageRect.left, cropBottom - cropTop)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(cropRight, cropTop),
                    size = Size(imageRect.right - cropRight, cropBottom - cropTop)
                )

                // Draw crop border
                drawRect(
                    color = Color.White,
                    topLeft = Offset(cropLeft, cropTop),
                    size = Size(cropRight - cropLeft, cropBottom - cropTop),
                    style = Stroke(width = 3f)
                )

                // Draw corner handles
                val handleRadius = 12f
                listOf(
                    Offset(cropLeft, cropTop),
                    Offset(cropRight, cropTop),
                    Offset(cropLeft, cropBottom),
                    Offset(cropRight, cropBottom)
                ).forEach { corner ->
                    drawCircle(Color.White, handleRadius, corner)
                    drawCircle(Color.Gray, handleRadius - 2f, corner)
                }
            }
        }
    }
}

private enum class CropHandle {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
}

private fun findCropHandle(
    position: Offset,
    cropRect: Rect,
    imageRect: Rect
): CropHandle? {
    val cropLeft = imageRect.left + cropRect.left * imageRect.width
    val cropTop = imageRect.top + cropRect.top * imageRect.height
    val cropRight = imageRect.left + cropRect.right * imageRect.width
    val cropBottom = imageRect.top + cropRect.bottom * imageRect.height
    
    val threshold = 50f
    
    return when {
        (position - Offset(cropLeft, cropTop)).getDistance() < threshold -> CropHandle.TOP_LEFT
        (position - Offset(cropRight, cropTop)).getDistance() < threshold -> CropHandle.TOP_RIGHT
        (position - Offset(cropLeft, cropBottom)).getDistance() < threshold -> CropHandle.BOTTOM_LEFT
        (position - Offset(cropRight, cropBottom)).getDistance() < threshold -> CropHandle.BOTTOM_RIGHT
        position.x in cropLeft..cropRight && position.y in cropTop..cropBottom -> CropHandle.CENTER
        else -> null
    }
}

private fun updateCropRect(
    handle: CropHandle,
    position: Offset,
    currentRect: Rect,
    imageRect: Rect
): Rect {
    // Convert position to normalized coordinates (0-1)
    val normalizedX = ((position.x - imageRect.left) / imageRect.width).coerceIn(0f, 1f)
    val normalizedY = ((position.y - imageRect.top) / imageRect.height).coerceIn(0f, 1f)
    
    val minSize = 0.1f
    
    return when (handle) {
        CropHandle.TOP_LEFT -> currentRect.copy(
            left = min(normalizedX, currentRect.right - minSize),
            top = min(normalizedY, currentRect.bottom - minSize)
        )
        CropHandle.TOP_RIGHT -> currentRect.copy(
            right = max(normalizedX, currentRect.left + minSize),
            top = min(normalizedY, currentRect.bottom - minSize)
        )
        CropHandle.BOTTOM_LEFT -> currentRect.copy(
            left = min(normalizedX, currentRect.right - minSize),
            bottom = max(normalizedY, currentRect.top + minSize)
        )
        CropHandle.BOTTOM_RIGHT -> currentRect.copy(
            right = max(normalizedX, currentRect.left + minSize),
            bottom = max(normalizedY, currentRect.top + minSize)
        )
        CropHandle.CENTER -> {
            val width = currentRect.width
            val height = currentRect.height
            val centerX = normalizedX.coerceIn(width / 2, 1f - width / 2)
            val centerY = normalizedY.coerceIn(height / 2, 1f - height / 2)
            Rect(
                centerX - width / 2,
                centerY - height / 2,
                centerX + width / 2,
                centerY + height / 2
            )
        }
    }
}


@Composable
private fun EditControlsBar(
    isGreyscale: Boolean,
    isCropping: Boolean,
    onGreyscaleToggle: () -> Unit,
    onCropToggle: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Greyscale toggle
        FilterChip(
            selected = isGreyscale,
            onClick = onGreyscaleToggle,
            label = { Text("Greyscale") },
            leadingIcon = {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(
                        id = android.R.drawable.ic_menu_gallery
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        // Crop toggle
        FilterChip(
            selected = isCropping,
            onClick = onCropToggle,
            label = { Text("Crop") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Crop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        // Reset button
        OutlinedButton(onClick = onReset) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("Reset")
        }
    }
}

// Extension functions for bitmap operations
private fun Context.loadBitmapFromUri(uri: Uri): Bitmap? {
    return try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        null
    }
}

private fun Bitmap.toGreyscale(): Bitmap {
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val paint = Paint()
    val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(this, 0f, 0f, paint)
    return result
}

private fun Bitmap.cropToRect(normalizedRect: Rect): Bitmap {
    val left = (normalizedRect.left * width).toInt().coerceIn(0, width)
    val top = (normalizedRect.top * height).toInt().coerceIn(0, height)
    val right = (normalizedRect.right * width).toInt().coerceIn(0, width)
    val bottom = (normalizedRect.bottom * height).toInt().coerceIn(0, height)
    
    val cropWidth = (right - left).coerceAtLeast(1)
    val cropHeight = (bottom - top).coerceAtLeast(1)
    
    return Bitmap.createBitmap(this, left, top, cropWidth, cropHeight)
}

private fun Context.saveBitmapToFile(bitmap: Bitmap): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "EDITED_${timeStamp}.jpg"
    val file = File(cacheDir, fileName)
    
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
    }
    
    return file
}

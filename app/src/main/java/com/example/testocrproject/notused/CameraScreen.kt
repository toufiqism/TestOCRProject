
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun CameraCaptureScreen(
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        val cameraProvider = getCameraProvider(context)
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val capture = ImageCapture.Builder()
            .setTargetResolution(Size(1280, 720))
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val selector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                capture
            )
            imageCapture = capture
        } catch (e: Exception) {
            Log.e("CameraCapture", "Camera binding failed", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = {
                imageCapture?.let { capture ->
                    takePhoto(context, capture, onImageCaptured)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Text("Capture")
        }
    }
}

private suspend fun getCameraProvider(context: Context): ProcessCameraProvider =
    withContext(Dispatchers.Main) {
        ProcessCameraProvider.getInstance(context).await()
    }

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Bitmap) -> Unit
) {
    val outputFile = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context), // ✅ no null here
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val bitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
                onImageCaptured(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraCapture", "Capture failed: ${exception.message}", exception)
            }
        }
    )
}
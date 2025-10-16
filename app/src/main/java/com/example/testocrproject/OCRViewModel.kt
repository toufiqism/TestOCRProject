package com.example.testocrproject

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class OCRViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext get() = getApplication<Application>().applicationContext
//    private val ocr = TesseractOCR(appContext)
//    var recognizedText = mutableStateOf("")
//
//    init {
//        copyTrainedData()
//        ocr.init(appContext.filesDir.absolutePath)
//    }
//
//    fun processImage(bitmap: Bitmap) {
//        viewModelScope.launch(Dispatchers.Default) {
//            val text = ocr.doOCR(bitmap)
//            recognizedText.value = text
//        }
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        ocr.destroy()
//    }
//
//    private fun copyTrainedData() {
//        val tessFolder = File(appContext.filesDir, "tessdata")
//        if (!tessFolder.exists()) tessFolder.mkdirs()
//
//        val trainedData = File(tessFolder, "ben.traineddata")
//        if (!trainedData.exists()) {
//            appContext.assets.open("tessdata/ben.traineddata").use { input ->
//                FileOutputStream(trainedData).use { output ->
//                    input.copyTo(output)
//                }
//            }
//        }
//    }
//
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return OCRViewModel(context.applicationContext as Application) as T
        }
    }

    private val _uiState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uiState = _uiState.asStateFlow()

    fun uploadImage(imageFile: File) {
        viewModelScope.launch {
            _uiState.value = UploadState.Loading
            try {
                val api = RetrofitInstance.getApi(appContext)
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                val response = api.uploadImage(body)

                if (response.isSuccessful) {
                    val extractedText = response.body()?.text
                    Log.d("OCR", "Extracted text: $extractedText")
                    _uiState.value = UploadState.Success(extractedText)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("OCR", "Upload failed: $errorBody")
                    _uiState.value = UploadState.Error("Upload failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("OCR", "Exception during upload", e)
                _uiState.value = UploadState.Error("Upload failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = UploadState.Idle
    }
}
// 4. Define UI State for the upload process
sealed interface UploadState {
    object Idle : UploadState
    object Loading : UploadState
    data class Success(val extractedText: String?) : UploadState
    data class Error(val message: String) : UploadState
}

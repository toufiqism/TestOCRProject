package com.example.testocrproject

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class OCRViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uiState: StateFlow<UploadState> = _uiState.asStateFlow()

    // Granular state flows for selective recomposition
    val isLoading = _uiState.map { it is UploadState.Loading }
    val isIdle = _uiState.map { it is UploadState.Idle }
    val isSuccess = _uiState.map { it is UploadState.Success }
    val isError = _uiState.map { it is UploadState.Error }
    
    val extractedText = _uiState.map { state ->
        (state as? UploadState.Success)?.extractedText
    }
    
    val errorMessage = _uiState.map { state ->
        (state as? UploadState.Error)?.message
    }

    fun uploadImage(imageFile: File, baseUrl: String) {
        viewModelScope.launch {
            _uiState.value = UploadState.Loading
            try {
                val api = RetrofitInstance.getApi(baseUrl)
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

                val response = api.uploadImage(body)

                if (response.isSuccessful) {
                    val extractedText = response.body()
                    Log.d("OCR", "Extracted text: $extractedText")
                    _uiState.value = UploadState.Success(extractedText)
                    
                    // Clean up the temporary file after successful upload
                    imageFile.delete()
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
    
    fun setError(message: String) {
        _uiState.value = UploadState.Error(message)
    }
}

// Define UI State for the upload process
sealed interface UploadState {
    data object Idle : UploadState
    data object Loading : UploadState
    data class Success(val extractedText: String?) : UploadState
    data class Error(val message: String) : UploadState
}

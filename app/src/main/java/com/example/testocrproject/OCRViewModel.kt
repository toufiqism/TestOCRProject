package com.example.testocrproject

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class OCRViewModel(private val appContext: Context) : ViewModel() {
    private val ocr = TesseractOCR(appContext)
    var recognizedText = mutableStateOf("")

    init {
        copyTrainedData()
        ocr.init(appContext.filesDir.absolutePath)
    }

    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            val text = ocr.doOCR(bitmap)
            recognizedText.value = text
        }
    }

    override fun onCleared() {
        super.onCleared()
        ocr.destroy()
    }

    private fun copyTrainedData() {
        val tessFolder = File(appContext.filesDir, "tessdata")
        if (!tessFolder.exists()) tessFolder.mkdirs()

        val trainedData = File(tessFolder, "ben.traineddata")
        if (!trainedData.exists()) {
            appContext.assets.open("tessdata/ben.traineddata").use { input ->
                FileOutputStream(trainedData).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return OCRViewModel(context.applicationContext) as T
        }
    }
}

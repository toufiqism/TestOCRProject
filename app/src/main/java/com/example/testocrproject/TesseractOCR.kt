package com.example.testocrproject

import android.content.Context
import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI

class TesseractOCR(private val context: Context) {
    private val tessBase = TessBaseAPI()

    fun init(dataPath: String) {
        tessBase.init(dataPath, "ben")
    }

    fun doOCR(bitmap: Bitmap): String {
        tessBase.setImage(bitmap)
        return tessBase.utF8Text ?: ""
    }

    fun destroy() {
        tessBase.end()
    }
}

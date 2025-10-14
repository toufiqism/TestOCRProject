package com.example.testocrproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.roundToInt

object ImagePreprocessor {
    /**
     * Simple preprocessing:
     * - Resize to reasonable size
     * - Convert to grayscale
     * - Apply simple threshold (Otsu-like)
     *
     * Tweak this for your dataset.
     */
    fun preprocessForTess(input: Bitmap): Bitmap {
        // scale so max dimension ~ 1200px (tesseract likes decent resolution)
        val maxDim = 1200
        val ratio = if (input.width >= input.height) {
            maxDim.toFloat() / input.width
        } else {
            maxDim.toFloat() / input.height
        }
        val targetW = (input.width * ratio).roundToInt().coerceAtLeast(1)
        val targetH = (input.height * ratio).roundToInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(input, targetW, targetH, true)

        val gray = toGrayscale(scaled)
        val thresh = simpleThreshold(gray)
        return thresh
    }

    private fun toGrayscale(src: Bitmap): Bitmap {
        val bmpGrayscale = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val matrix = android.graphics.ColorMatrix()
        matrix.setSaturation(0f)
        val filter = android.graphics.ColorMatrixColorFilter(matrix)
        paint.colorFilter = filter
        c.drawBitmap(src, 0f, 0f, paint)
        return bmpGrayscale
    }

    private fun simpleThreshold(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val dst = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Compute global average as threshold (fast, simple)
        var sum = 0L
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)
        for (px in pixels) {
            val r = Color.red(px)
            sum += r
        }
        val avg = (sum / pixels.size).toInt()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = src.getPixel(x, y)
                val r = Color.red(p)
                val out = if (r < avg) Color.BLACK else Color.WHITE
                dst.setPixel(x, y, out)
            }
        }
        return dst
    }
}

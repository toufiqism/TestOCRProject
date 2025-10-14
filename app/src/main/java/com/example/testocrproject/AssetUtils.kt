package com.example.testocrproject

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object AssetUtils {

    fun copyTessDataIfNeeded(context: Context, destPath: String) {
        val assetManager = context.assets
        val tessFolder = File(destPath, "tessdata")
        if (!tessFolder.exists()) tessFolder.mkdirs()
        val assetList = assetManager.list("tessdata") ?: return

        for (fileName in assetList) {
            val destFile = File(tessFolder, fileName)
            if (!destFile.exists()) {
                assetManager.open("tessdata/$fileName").use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}

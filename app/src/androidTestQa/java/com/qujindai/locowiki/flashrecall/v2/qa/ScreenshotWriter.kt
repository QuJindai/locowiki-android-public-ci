package com.qujindai.locowiki.flashrecall.v2.qa

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

object ScreenshotWriter {
    fun capture(node: SemanticsNodeInteraction, name: String): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "qa/screenshots").also { it.mkdirs() }
        val output = File(directory, "$name.png")
        output.outputStream().use { stream ->
            check(node.captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
        return output
    }
}

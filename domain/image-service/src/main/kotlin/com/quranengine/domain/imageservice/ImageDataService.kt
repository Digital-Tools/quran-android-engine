package com.quranengine.domain.imageservice

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.quranengine.data.wordframe.WordFramePersistence
import com.quranengine.domain.wordframeservice.WordFrameProcessor
import com.quranengine.model.qurangeometry.AyahNumberLocation
import com.quranengine.model.qurangeometry.ImagePage
import com.quranengine.model.qurangeometry.SuraHeaderLocation
import com.quranengine.model.qurangeometry.WordFrameCollection
import com.quranengine.model.qurankit.Page
import com.quranengine.core.utilities.extensions.as3DigitString
import timber.log.Timber
import java.io.File

class ImageDataService(
    private val persistence: WordFramePersistence,
    private val imagesDirectory: File,
) {
    private val processor = WordFrameProcessor()

    suspend fun suraHeaders(page: Page): List<SuraHeaderLocation> =
        persistence.suraHeaders(page)

    suspend fun ayahNumbers(page: Page): List<AyahNumberLocation> =
        persistence.ayahNumbers(page)

    suspend fun imageForPage(page: Page): ImagePage {
        val imageFile = imageFileForPage(page)
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, options)
        if (bitmap == null) {
            logFiles(imagesDirectory)
            imagesDirectory.parentFile?.let { logFiles(it) }
            imagesDirectory.parentFile?.parentFile?.let { logFiles(it) }
            error("No image found for page '${page.pageNumber}'")
        }

        return ImagePage(page = page, image = bitmap)
    }

    suspend fun wordFrames(page: Page): WordFrameCollection {
        val plainWordFrames = persistence.wordFrameCollectionForPage(page)
        return processor.processWordFrames(plainWordFrames)
    }

    private fun logFiles(directory: File) {
        val files = directory.listFiles()?.map { it.name } ?: emptyList()
        Timber.e("Images: Directory %s contains files %s", directory, files)
    }

    private fun imageFileForPage(page: Page): File =
        File(imagesDirectory, "page${page.pageNumber.as3DigitString()}.png")
}

package com.quranengine.domain.reciterservice

import com.quranengine.core.system.FileSystem
import com.quranengine.model.quranaudio.AudioDownloadedSize
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.quranaudio.localFolder
import com.quranengine.model.qurankit.Quran
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.io.File

class ReciterSizeInfoRetriever(
    private val baseURL: String,
    private val fileSystem: FileSystem,
    private val baseDir: File,
) {

    suspend fun getDownloadedSizes(
        reciters: List<Reciter>,
        quran: Quran,
    ): Map<Reciter, AudioDownloadedSize> = coroutineScope {
        val deferred = reciters.map { reciter ->
            async { reciter to getDownloadedSize(reciter, quran) }
        }
        deferred.associate { it.await() }
    }

    suspend fun getDownloadedSize(reciter: Reciter, quran: Quran): AudioDownloadedSize {
        val fileList = reciter.audioFiles(baseURL, from = quran.firstVerse, to = quran.lastVerse)

        val folderPath = File(baseDir, reciter.localFolder())
        val fileEntries = try {
            fileSystem.contentsOfDirectory(folderPath)
        } catch (_: Exception) {
            return AudioDownloadedSize.zero(quran)
        }

        val sizeInBytes = sizeInBytes(fileEntries)

        // Remove suras for which we didn't find downloaded files
        val downloadedNames = fileEntries.map { it.name }.toSet()
        val notDownloaded = fileList.filter { file ->
            !downloadedNames.contains(File(file.local).name)
        }
        val downloadedSuras = quran.suras.toMutableSet()
        for (file in notDownloaded) {
            val sura = file.sura ?: continue
            downloadedSuras.remove(sura)
        }

        return AudioDownloadedSize(
            downloadedSizeInBytes = sizeInBytes,
            downloadedSuraCount = downloadedSuras.size,
            surasCount = quran.suras.size,
        )
    }

    private fun sizeInBytes(files: List<File>): Long {
        var total = 0L
        for (file in files) {
            try {
                val size = fileSystem.fileSize(file)
                if (size != null) total += size
            } catch (e: Exception) {
                Timber.e("Unexpected error while getting file size. Error: %s", e.message)
            }
        }
        return total
    }
}

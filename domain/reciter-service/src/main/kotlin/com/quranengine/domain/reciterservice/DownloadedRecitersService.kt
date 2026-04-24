package com.quranengine.domain.reciterservice

import com.quranengine.core.system.FileSystem
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.quranaudio.audioFilesPath
import com.quranengine.model.quranaudio.isReciterDirectory
import com.quranengine.model.quranaudio.localFolder
import java.io.File

class DownloadedRecitersService(
    private val fileSystem: FileSystem,
    private val baseDir: File,
) {

    fun downloadedReciters(allReciters: List<Reciter>): List<Reciter> {
        val audioDir = File(baseDir, Reciter.audioFilesPath)
        val downloadedDirs = try {
            fileSystem.contentsOfDirectory(audioDir)
        } catch (_: Exception) {
            return emptyList()
        }

        return allReciters.filter { reciter ->
            downloadedDirs.any { dir -> isDownloadedReciter(reciter, dir) }
        }
    }

    private fun isDownloadedReciter(reciter: Reciter, downloadedReciterDir: File): Boolean {
        if (!reciter.isReciterDirectory(downloadedReciterDir.name)) return false
        return try {
            fileSystem.contentsOfDirectory(downloadedReciterDir).isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }
}

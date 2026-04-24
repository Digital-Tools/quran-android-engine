package com.quranengine.domain.reciterservice

import com.quranengine.core.system.FileSystem
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.quranaudio.localFolder
import java.io.File

class ReciterAudioDeleter(
    private val fileSystem: FileSystem,
    private val baseDir: File,
) {

    suspend fun deleteAudioFiles(reciter: Reciter) {
        val folder = File(baseDir, reciter.localFolder())
        fileSystem.removeItem(folder)
    }
}

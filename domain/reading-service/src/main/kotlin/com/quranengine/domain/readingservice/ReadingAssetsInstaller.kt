package com.quranengine.domain.readingservice

import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.SystemBundle
import com.quranengine.model.qurankit.Reading
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ReadingAssetsInstaller(
    private val systemBundle: SystemBundle,
    private val fileSystem: FileSystem,
    private val baseDir: File,
) {

    suspend fun ensureInstalled(reading: Reading) = withContext(Dispatchers.IO) {
        val assetRoot = reading.bundledAssetsPath
        if (systemBundle.listAssets(assetRoot).isEmpty()) {
            Timber.i("Reading assets: no bundled assets found for %s", reading.localPath)
            return@withContext
        }

        val readingRoot = File(baseDir, assetRoot)
        if (isInstalled(reading, readingRoot)) {
            return@withContext
        }

        val tempRoot = File(baseDir, "readings/.${reading.localPath}.installing")
        if (tempRoot.exists()) {
            fileSystem.removeItem(tempRoot)
        }
        if (readingRoot.exists()) {
            fileSystem.removeItem(readingRoot)
        }

        fileSystem.createDirectory(tempRoot, withIntermediateDirectories = true)
        copyAssetTree(assetRoot, tempRoot)
        File(tempRoot, INSTALL_MARKER).writeText(INSTALL_VERSION)
        fileSystem.moveItem(tempRoot, readingRoot)
        Timber.i("Reading assets: installed bundled assets for %s", reading.localPath)
    }

    private fun isInstalled(reading: Reading, readingRoot: File): Boolean {
        if (!File(readingRoot, INSTALL_MARKER).isFile) return false

        val resources = reading.imageResources
        val requiredFiles = listOf(
            resources.databasePath,
            "${resources.imagesPath}/page001.png",
            "${resources.imagesPath}/page604.png",
        )
        return requiredFiles.all { File(readingRoot, it).isFile }
    }

    private fun copyAssetTree(assetPath: String, destination: File) {
        val children = systemBundle.listAssets(assetPath)
        if (children.isEmpty()) {
            val input = systemBundle.openAsset(assetPath)
                ?: error("Bundled reading asset is missing: $assetPath")
            destination.parentFile?.let { parent ->
                if (!parent.exists()) {
                    fileSystem.createDirectory(parent, withIntermediateDirectories = true)
                }
            }
            input.use { source ->
                destination.outputStream().use { target ->
                    source.copyTo(target)
                }
            }
            return
        }

        if (!destination.exists()) {
            fileSystem.createDirectory(destination, withIntermediateDirectories = true)
        }
        for (child in children) {
            copyAssetTree("$assetPath/$child", File(destination, child))
        }
    }

    private companion object {
        const val INSTALL_MARKER = ".installed-from-assets"
        const val INSTALL_VERSION = "1"
    }
}

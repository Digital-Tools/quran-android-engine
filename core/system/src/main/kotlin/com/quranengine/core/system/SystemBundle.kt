package com.quranengine.core.system

import android.content.Context
import android.content.pm.PackageManager
import org.json.JSONArray
import java.io.File
import java.io.InputStream

/**
 * Abstraction over application bundle / resource access.
 * Port of Swift's `SystemBundle` protocol.
 */
interface SystemBundle {
    /** Reads a JSON array from an asset or raw resource file. */
    fun readArray(resource: String, extension: String): List<Any?>

    /** Returns an application-level metadata value (e.g., version name). */
    fun infoValue(key: String): Any?

    /** Opens an [InputStream] for the named asset, or `null` if not found. */
    fun openAsset(name: String): InputStream?

    /** Lists the child asset names for a directory path. */
    fun listAssets(path: String): List<String>
}

/**
 * Default implementation backed by Android [Context].
 */
class DefaultSystemBundle(private val context: Context) : SystemBundle {

    override fun readArray(resource: String, extension: String): List<Any?> {
        val fileName = "$resource.$extension"
        val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(text)
        return (0 until jsonArray.length()).map { jsonArray.opt(it) }
    }

    override fun infoValue(key: String): Any? = when (key) {
        "versionName" -> {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName
        }
        "versionCode" -> {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
        }
        else -> {
            val info = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA,
            )
            info.metaData?.get(key)
        }
    }

    override fun openAsset(name: String): InputStream? =
        try {
            context.assets.open(name)
        } catch (_: Exception) {
            null
        }

    override fun listAssets(path: String): List<String> =
        try {
            context.assets.list(path)?.toList().orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
}

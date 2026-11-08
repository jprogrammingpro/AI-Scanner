package com.jprogrammingpro.aiscanner.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Stores page images inside the app's private files directory. */
class PageStorage(private val context: Context) {
    private val dir: File get() = File(context.filesDir, "pages").apply { mkdirs() }

    fun newPageFile(): File = File(dir, "${UUID.randomUUID()}.jpg")

    suspend fun importFrom(uri: Uri): File? = withContext(Dispatchers.IO) {
        val target = newPageFile()
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { input.copyTo(it) }
            } ?: return@withContext null
            target
        }.getOrNull()
    }

    suspend fun delete(paths: List<String>) = withContext(Dispatchers.IO) {
        paths.forEach { runCatching { File(it).delete() } }
    }
}

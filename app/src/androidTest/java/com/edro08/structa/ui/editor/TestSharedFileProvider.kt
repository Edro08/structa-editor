package com.edro08.structa.ui.editor

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/** A non-DocumentsProvider content URI like one sent by a file-sharing app. */
class TestSharedFileProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    private fun name(uri: Uri): String = uri.lastPathSegment.orEmpty()
    private fun content(uri: Uri): ByteArray = ("content for " + name(uri) + "\n").toByteArray(Charsets.UTF_8)

    override fun getType(uri: Uri): String =
        if (name(uri).endsWith(".json")) "application/json" else "text/plain"

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        val cursor = MatrixCursor(columns)
        val row = cursor.newRow()
        for (column in columns) {
            when (column) {
                OpenableColumns.DISPLAY_NAME -> row.add(name(uri))
                OpenableColumns.SIZE -> row.add(content(uri).size)
                else -> row.add(null)
            }
        }
        return cursor
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw FileNotFoundException("Read only")
        val context = context ?: throw FileNotFoundException("Missing context")
        val file = File(context.cacheDir, "shared-" + name(uri))
        try {
            FileOutputStream(file).use { it.write(content(uri)) }
        } catch (e: IOException) {
            throw FileNotFoundException(e.message)
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException()
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException()
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException()
}

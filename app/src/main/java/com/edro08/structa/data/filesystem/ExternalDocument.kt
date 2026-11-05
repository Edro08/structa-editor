package com.edro08.structa.data.filesystem

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.edro08.structa.R
import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.document.MAX_EDITABLE_BYTES
import com.edro08.structa.domain.editor.encoding.CharsetDetector
import com.edro08.structa.domain.editor.encoding.DecodedContent
import com.edro08.structa.domain.editor.encoding.DocumentCharset
import com.edro08.structa.domain.filesystem.FileEntry
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun validateExternalUri(context: Context, uri: Uri) {
    if (uri.scheme != "content") {
        throw IOException("Solo se admiten documentos compartidos mediante content://")
    }
    if (uri.authority.equals("${context.packageName}.fileprovider", ignoreCase = true)) {
        throw IOException("No se permite abrir documentos internos del propio proveedor")
    }
}

/** Metadata for both SAF documents and content:// URIs from other apps (e.g. FileProvider). */
suspend fun externalDocument(context: Context, uri: Uri): FileEntry = withContext(Dispatchers.IO) {
    validateExternalUri(context, uri)
    val resolver = context.contentResolver
    val cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        ?: throw IOException("No se pudo consultar el archivo")
    cursor.use {
        if (!it.moveToFirst()) throw IOException("El archivo ya no está disponible")
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
        val name = if (nameIndex >= 0 && !it.isNull(nameIndex)) it.getString(nameIndex)
            else uri.lastPathSegment ?: context.getString(R.string.editor_untitled)
        val size = if (sizeIndex >= 0 && !it.isNull(sizeIndex)) it.getLong(sizeIndex) else 0L
        FileEntry(DocumentId(uri.toString()), name, size,
            resolver.getType(uri) == DocumentsContract.Document.MIME_TYPE_DIR)
    }
}

suspend fun readExternalDocument(
    context: Context,
    id: DocumentId,
    explicitCharset: DocumentCharset?
): DecodedContent = withContext(Dispatchers.IO) {
    val uri = Uri.parse(id.value)
    validateExternalUri(context, uri)
    val input = context.contentResolver.openInputStream(uri)
        ?: throw IOException("No se pudo abrir el flujo de lectura del documento externo")
    val bytes = input.use { stream ->
        val buffer = ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        var total = 0L
        while (true) {
            val read = stream.read(chunk)
            if (read < 0) break
            total += read
            if (total > MAX_EDITABLE_BYTES) {
                throw IOException("Este archivo supera los 25 MB y aun no puede editarse.")
            }
            buffer.write(chunk, 0, read)
        }
        buffer.toByteArray()
    }
    if (explicitCharset == null && CharsetDetector.isBinary(bytes)) {
        throw IOException("El archivo parece ser binario y no puede editarse como texto.")
    }
    CharsetDetector.decode(bytes, explicitCharset)
}

suspend fun writeExternalDocument(
    context: Context,
    destination: DocumentId,
    content: String,
    charset: DocumentCharset
): Unit = withContext(Dispatchers.IO) {
    val uri = Uri.parse(destination.value)
    validateExternalUri(context, uri)
    val bytes = CharsetDetector.encode(content, charset)
    val output = context.contentResolver.openOutputStream(uri, "wt")
        ?: throw IOException("No se pudo escribir en el documento externo")
    output.use { stream ->
        stream.write(bytes)
        stream.flush()
    }
}

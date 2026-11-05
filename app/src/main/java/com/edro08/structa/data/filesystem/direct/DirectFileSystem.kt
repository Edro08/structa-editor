package com.edro08.structa.data.filesystem.direct

import android.content.Context
import android.net.Uri
import com.edro08.structa.data.filesystem.externalDocument
import com.edro08.structa.data.filesystem.readExternalDocument
import com.edro08.structa.data.filesystem.writeExternalDocument
import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.document.MAX_EDITABLE_BYTES
import com.edro08.structa.domain.editor.encoding.CharsetDetector
import com.edro08.structa.domain.editor.encoding.DecodedContent
import com.edro08.structa.domain.editor.encoding.DocumentCharset
import com.edro08.structa.domain.filesystem.FileEntry
import com.edro08.structa.domain.filesystem.FileSystem
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Shared storage via real paths; never traverses Android/data or Android/obb. */
class DirectFileSystem(private val context: Context? = null) : FileSystem {
    override val key = "direct"

    private fun isContentUri(id: DocumentId): Boolean =
        context != null && id.value.startsWith("content://")

    private fun file(id: DocumentId): File {
        if (!File(id.value).isAbsolute) throw IOException("La ruta de la carpeta no es válida")
        val resolved = File(id.value).canonicalFile
        val parts = resolved.path.split(File.separatorChar)
        if (parts.windowed(2).any { it[0].equals("Android", true) &&
                (it[1].equals("data", true) || it[1].equals("obb", true)) })
            throw IOException("Android/data y Android/obb no están disponibles con Acceso completo")
        return resolved
    }

    private fun entry(file: File): FileEntry {
        if (!file.exists()) throw IOException("El archivo ya no existe")
        return FileEntry(DocumentId(file.canonicalPath), file.name.ifBlank { file.path },
            if (file.isDirectory) 0 else file.length(), file.isDirectory)
    }

    override suspend fun stat(id: DocumentId): FileEntry = withContext(Dispatchers.IO) {
        if (isContentUri(id)) externalDocument(context!!, Uri.parse(id.value))
        else entry(file(id))
    }

    override suspend fun exists(id: DocumentId): Boolean = withContext(Dispatchers.IO) {
        if (isContentUri(id)) {
            try {
                externalDocument(context!!, Uri.parse(id.value))
                true
            } catch (_: Exception) {
                false
            }
        } else {
            file(id).exists()
        }
    }

    override suspend fun list(directory: DocumentId): List<FileEntry> = withContext(Dispatchers.IO) {
        val folder = file(directory)
        if (!folder.isDirectory) throw IOException("La carpeta no existe o no se puede leer")
        val children = folder.listFiles() ?: throw IOException("No se puede leer la carpeta")
        children.filter { child ->
            // Restricted directories may be shown, but may not be entered.
            try { file(DocumentId(child.path)); true } catch (_: IOException) { false }
        }.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }).map(::entry)
    }

    override suspend fun read(file: FileEntry): String = readWithCharset(file).text

    override suspend fun readWithCharset(file: FileEntry, explicitCharset: DocumentCharset?): DecodedContent = withContext(Dispatchers.IO) {
        if (isContentUri(file.id)) {
            return@withContext readExternalDocument(context!!, file.id, explicitCharset)
        }
        val source = file(file.id)
        if (!source.isFile) throw IOException("No se puede leer una carpeta como archivo")
        if (source.length() > MAX_EDITABLE_BYTES) throw IOException("Este archivo supera los 25 MB y aun no puede editarse.")
        val bytes = source.readBytes()
        if (explicitCharset == null && CharsetDetector.isBinary(bytes)) {
            throw IOException("El archivo parece ser binario y no puede editarse como texto.")
        }
        CharsetDetector.decode(bytes, explicitCharset)
    }

    override suspend fun write(destination: DocumentId, content: String): Unit =
        writeWithCharset(destination, content, DocumentCharset.DEFAULT)

    override suspend fun writeWithCharset(destination: DocumentId, content: String, charset: DocumentCharset): Unit = withContext(Dispatchers.IO) {
        if (isContentUri(destination)) {
            writeExternalDocument(context!!, destination, content, charset)
            return@withContext
        }
        val target = file(destination)
        validateName(target.name)
        if (target.exists() && target.isDirectory) throw IOException("No se puede escribir en una carpeta")
        val parent = target.parentFile ?: throw IOException("La ruta de destino no tiene carpeta contenedora")
        if (!parent.exists() && !parent.mkdirs()) throw IOException("No se pudo crear la carpeta contenedora")
        val bytes = CharsetDetector.encode(content, charset)
        val tempFile = File(parent, ".${target.name}.${System.nanoTime()}.tmp")
        try {
            tempFile.outputStream().use { out ->
                out.write(bytes)
                try { out.fd.sync() } catch (_: Exception) {}
            }
            if (!tempFile.renameTo(target)) {
                target.writeBytes(bytes)
                tempFile.delete()
            }
        } catch (e: Exception) {
            tempFile.delete()
            throw if (e is IOException) e else IOException("No se pudo guardar el archivo", e)
        }
    }

    override suspend fun create(parent: DocumentId, name: String, directory: Boolean): FileEntry = withContext(Dispatchers.IO) {
        validateName(name)
        val folder = file(parent)
        if (!folder.isDirectory) throw IOException("La carpeta no existe")
        val candidate = File(folder, name)
        val target = file(DocumentId(candidate.path))
        if (target.parentFile?.canonicalPath != folder.canonicalPath) {
            throw IOException("El elemento debe crearse dentro de la carpeta contenedora")
        }
        if (target.exists()) throw IOException("Ya existe un archivo con ese nombre")
        if (directory) {
            if (!target.mkdir()) throw IOException("No se pudo crear la carpeta")
        } else if (!target.createNewFile()) throw IOException("No se pudo crear el archivo")
        entry(target)
    }

    override suspend fun rename(file: DocumentId, name: String): FileEntry = withContext(Dispatchers.IO) {
        validateName(name)
        val source = file(file)
        if (!source.exists()) throw IOException("El archivo ya no existe")
        val candidate = File(source.parentFile, name)
        val target = file(DocumentId(candidate.path))
        if (target.parentFile?.canonicalPath != source.parentFile?.canonicalPath) {
            throw IOException("No se permite mover el archivo fuera de su directorio de origen")
        }
        if (target != source && target.exists()) throw IOException("Ya existe un archivo con ese nombre")
        if (target != source && !source.renameTo(target)) throw IOException("No se pudo renombrar el archivo")
        entry(target)
    }

    override suspend fun delete(file: DocumentId): Unit = withContext(Dispatchers.IO) {
        val target = file(file)
        if (!target.delete()) throw IOException("No se pudo eliminar el archivo (las carpetas deben estar vacías)")
    }

    companion object {
        fun isValidName(name: String): Boolean =
            name.isNotBlank() && name != "." && name != ".." && !name.contains("..") && name.none {
                it == '/' || it == '\\' || it == '\u0000' || it < ' '
            }

        fun validateName(name: String) {
            require(isValidName(name)) { "Nombre de archivo no válido" }
        }
    }
}

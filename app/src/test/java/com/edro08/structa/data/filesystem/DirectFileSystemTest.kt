package com.edro08.structa.data.filesystem

import com.edro08.structa.data.filesystem.direct.DirectFileRef
import com.edro08.structa.data.filesystem.direct.DirectFileSystem
import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.filesystem.FileRef
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DirectFileSystemTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun createsListsRenamesAndDeletesEntriesViaRealPaths() {
        runBlocking {
            val fs = DirectFileSystem()
            val root = DirectFileRef(temporary.root.path).ref
            assertEquals("direct", root.fileSystem)
            assertEquals(FileRef.of(root.id), root)
            val folder = fs.create(root.id, "Documents", true)
            val original = fs.create(folder.id, "text.txt", false)
            fs.write(original.id, "uno\ndos")
            assertEquals("uno\ndos", fs.read(fs.stat(original.id)))
            assertEquals(listOf("text.txt"), fs.list(folder.id).map { it.name })
            val renamed = fs.rename(original.id, "nuevo.txt")
            assertFalse(fs.exists(original.id))
            assertEquals("uno\ndos", fs.read(renamed))
            assertTrue(fs.exists(renamed.id))
            assertThrows(IOException::class.java) { runBlocking { fs.create(folder.id, "nuevo.txt", false) } }
            fs.delete(renamed.id)
            fs.delete(folder.id)
            assertFalse(fs.exists(folder.id))
        }
    }

    @Test fun refusesRestrictedRoutesAndPathTraversal() {
        runBlocking {
            val fs = DirectFileSystem()
            val root = DocumentId(temporary.root.canonicalPath)
            assertThrows(IllegalArgumentException::class.java) {
                runBlocking { fs.create(root, "../outside", false) }
            }
            val android = fs.create(root, "Android", true)
            assertThrows(IOException::class.java) {
                runBlocking { fs.create(android.id, "data", true) }
            }
            assertFalse(java.io.File(temporary.root, "Android/data").exists())
            assertThrows(IllegalArgumentException::class.java) { DirectFileRef("relative") }
            assertThrows(IllegalArgumentException::class.java) {
                runBlocking { fs.create(root, "name\nwith\nnewline", false) }
            }
            assertThrows(IllegalArgumentException::class.java) {
                runBlocking { fs.create(root, "name..withdots", false) }
            }
        }
    }

    @Test fun rejectsBinaryFileReading() {
        runBlocking {
            val fs = DirectFileSystem()
            val root = DocumentId(temporary.root.canonicalPath)
            val binaryFile = fs.create(root, "sample.bin", false)
            java.io.File(binaryFile.id.value).writeBytes(byteArrayOf(0x7F, 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte(), 0x00))
            val exception = assertThrows(IOException::class.java) {
                runBlocking { fs.read(fs.stat(binaryFile.id)) }
            }
            assertTrue(exception.message?.contains("binario") == true)
        }
    }

    @Test fun atomicWritePreservesCharsetAndCleansTempFiles() {
        runBlocking {
            val fs = DirectFileSystem()
            val root = DocumentId(temporary.root.canonicalPath)
            val target = fs.create(root, "utf16.txt", false)
            fs.writeWithCharset(target.id, "Hola Mundo", com.edro08.structa.domain.editor.encoding.DocumentCharset.UTF_16_LE)
            val decoded = fs.readWithCharset(fs.stat(target.id), null)
            assertEquals("Hola Mundo", decoded.text)
            assertEquals(com.edro08.structa.domain.editor.encoding.DocumentCharset.UTF_16_LE, decoded.charset)
            assertTrue(temporary.root.listFiles().orEmpty().none { it.name.endsWith(".tmp") })
        }
    }
}

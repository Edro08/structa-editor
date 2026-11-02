package com.edro08.structa.application.search

import com.edro08.structa.application.browser.ListDirectory
import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.filesystem.DirectoryReader
import com.edro08.structa.domain.filesystem.FileEntry
import com.edro08.structa.domain.filesystem.FileReader
import com.edro08.structa.domain.filesystem.FileWriter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchWorkspaceTest {

    @Test
    fun searchesRecursivelyAndFiltersExclusions() = runTest {
        val root = DocumentId("root")
        val srcDir = DocumentId("root/src")
        val gitDir = DocumentId("root/.git")

        val files = mapOf(
            root to listOf(
                FileEntry(srcDir, "src", sizeBytes = 0L, isDirectory = true),
                FileEntry(gitDir, ".git", sizeBytes = 0L, isDirectory = true),
                FileEntry(DocumentId("root/README.md"), "README.md", sizeBytes = 50L, isDirectory = false)
            ),
            srcDir to listOf(
                FileEntry(DocumentId("root/src/Main.kt"), "Main.kt", sizeBytes = 100L, isDirectory = false),
                FileEntry(DocumentId("root/src/Utils.kt"), "Utils.kt", sizeBytes = 80L, isDirectory = false)
            ),
            gitDir to listOf(
                FileEntry(DocumentId("root/.git/config"), "config", sizeBytes = 20L, isDirectory = false)
            )
        )

        val contents = mapOf(
            DocumentId("root/README.md") to "# Structa\nA mobile editor.",
            DocumentId("root/src/Main.kt") to "fun main() {\n    println(\"Structa\")\n}",
            DocumentId("root/src/Utils.kt") to "val name = \"other\"",
            DocumentId("root/.git/config") to "Structa config ignored"
        )

        val dirReader = object : DirectoryReader {
            override suspend fun list(directory: DocumentId) = files[directory] ?: emptyList()
        }
        val fileReader = object : FileReader {
            override suspend fun read(file: FileEntry) = contents[file.id] ?: ""
        }

        val searcher = SearchWorkspace(ListDirectory(dirReader), fileReader)
        val result = searcher.search(root, "Structa", caseSensitive = false)

        assertEquals(2, result.matches.size)
        val matchedFiles = result.matches.map { it.file.entry.name }
        assertTrue("README.md" in matchedFiles)
        assertTrue("Main.kt" in matchedFiles)
        // .git must NOT be searched
        assertTrue("config" !in matchedFiles)
    }

    @Test
    fun batchReplaceInFilesWorksCorrectly() = runTest {
        val fileId = DocumentId("file1.kt")
        val written = mutableMapOf<DocumentId, String>()

        val fileEntry = FileEntry(fileId, "file1.kt", sizeBytes = 50L, isDirectory = false)
        val dirReader = object : DirectoryReader {
            override suspend fun list(directory: DocumentId) = listOf(fileEntry)
        }
        val fileReader = object : FileReader {
            override suspend fun read(file: FileEntry) = "val foo = 1\nval fooBar = 2"
        }
        val fileWriter = object : FileWriter {
            override suspend fun write(destination: DocumentId, content: String) {
                written[destination] = content
            }
        }

        val searcher = SearchWorkspace(ListDirectory(dirReader), fileReader, fileWriter)
        val searchResult = searcher.search(DocumentId("root"), "foo", caseSensitive = true)
        assertEquals(2, searchResult.matches.size)

        val replacedCount = searcher.replaceInFiles(searchResult.matches, "baz")
        assertEquals(2, replacedCount)
        assertEquals("val baz = 1\nval bazBar = 2", written[fileId])
    }
}

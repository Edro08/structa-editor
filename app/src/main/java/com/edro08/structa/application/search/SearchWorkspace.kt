package com.edro08.structa.application.search

import com.edro08.structa.application.browser.ListDirectory
import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.filesystem.FileReader
import com.edro08.structa.domain.filesystem.FileWriter
import com.edro08.structa.domain.workspace.IndexedFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

data class WorkspaceSearchMatch(
    val file: IndexedFile,
    val line: Int,
    val lineContent: String,
    val matchStartInLine: Int,
    val matchEndInLine: Int
)

data class WorkspaceSearchResult(
    val query: String,
    val matches: List<WorkspaceSearchMatch>,
    val filesSearched: Int,
    val truncated: Boolean = false
)

class SearchWorkspace(
    private val listDirectory: ListDirectory,
    private val fileReader: FileReader,
    private val fileWriter: FileWriter? = null
) {
    companion object {
        val EXCLUDED_DIRS = setOf(
            ".git", "build", ".gradle", "node_modules", ".idea", ".cxx",
            "dist", "out", "target", "bin", ".cache", "__pycache__", ".svn", ".hg"
        )
        const val MAX_FILE_SIZE_BYTES = 2_000_000L // 2MB limit per file for global search
        const val MAX_MATCHES = 1_000
    }

    suspend fun search(
        root: DocumentId,
        query: String,
        caseSensitive: Boolean = false,
        onProgress: ((Int, Int) -> Unit)? = null
    ): WorkspaceSearchResult {
        if (query.isEmpty()) return WorkspaceSearchResult(query, emptyList(), 0)

        val queue = ArrayDeque<Pair<DocumentId, String>>()
        val visited = mutableSetOf<DocumentId>()
        val matches = mutableListOf<WorkspaceSearchMatch>()
        var filesSearched = 0
        var truncated = false

        queue.add(root to "")

        while (queue.isNotEmpty() && matches.size < MAX_MATCHES) {
            coroutineContext.ensureActive()
            val (dirId, relativeDir) = queue.removeFirst()
            if (!visited.add(dirId)) continue

            val entries = try {
                listDirectory(dirId)
            } catch (c: CancellationException) {
                throw c
            } catch (_: Exception) {
                continue
            }

            for (entry in entries) {
                coroutineContext.ensureActive()
                if (matches.size >= MAX_MATCHES) {
                    truncated = true
                    break
                }
                val relativePath = if (relativeDir.isEmpty()) entry.name else "$relativeDir/${entry.name}"

                if (entry.isDirectory) {
                    if (entry.name !in EXCLUDED_DIRS && !entry.name.startsWith(".")) {
                        queue.add(entry.id to relativePath)
                    }
                } else {
                    if (entry.sizeBytes <= MAX_FILE_SIZE_BYTES) {
                        filesSearched++
                        val content = try {
                            fileReader.read(entry)
                        } catch (c: CancellationException) {
                            throw c
                        } catch (_: Exception) {
                            continue
                        }

                        val indexedFile = IndexedFile(entry, relativePath)
                        searchInContent(indexedFile, content, query, caseSensitive, matches)
                        onProgress?.invoke(filesSearched, matches.size)
                    }
                }
            }
        }

        return WorkspaceSearchResult(
            query = query,
            matches = matches,
            filesSearched = filesSearched,
            truncated = truncated || matches.size >= MAX_MATCHES
        )
    }

    private fun searchInContent(
        file: IndexedFile,
        content: String,
        query: String,
        caseSensitive: Boolean,
        outMatches: MutableList<WorkspaceSearchMatch>
    ) {
        val lines = content.lines()
        for ((lineIdx, lineText) in lines.withIndex()) {
            if (outMatches.size >= MAX_MATCHES) return
            var startIndex = 0
            while (startIndex < lineText.length) {
                val matchIdx = lineText.indexOf(query, startIndex, ignoreCase = !caseSensitive)
                if (matchIdx == -1) break
                outMatches.add(
                    WorkspaceSearchMatch(
                        file = file,
                        line = lineIdx,
                        lineContent = lineText.trimEnd('\r', '\n'),
                        matchStartInLine = matchIdx,
                        matchEndInLine = matchIdx + query.length
                    )
                )
                startIndex = matchIdx + query.length
                if (outMatches.size >= MAX_MATCHES) return
            }
        }
    }

    suspend fun replaceInFiles(
        matches: List<WorkspaceSearchMatch>,
        replacement: String
    ): Int {
        val writer = fileWriter ?: return 0
        var replacedCount = 0
        val groupedByFile = matches.groupBy { it.file }

        for ((file, fileMatches) in groupedByFile) {
            coroutineContext.ensureActive()
            val content = try {
                fileReader.read(file.entry)
            } catch (_: Exception) {
                continue
            }

            val lines = content.lines().toMutableList()
            val matchesByLine = fileMatches.groupBy { it.line }

            for ((lineIdx, lineMatches) in matchesByLine) {
                if (lineIdx < lines.size) {
                    var currentLine = lines[lineIdx]
                    val sorted = lineMatches.sortedByDescending { it.matchStartInLine }
                    for (m in sorted) {
                        if (m.matchEndInLine <= currentLine.length) {
                            currentLine = currentLine.substring(0, m.matchStartInLine) +
                                replacement + currentLine.substring(m.matchEndInLine)
                            replacedCount++
                        }
                    }
                    lines[lineIdx] = currentLine
                }
            }

            val newContent = lines.joinToString("\n")
            try {
                writer.write(file.entry.id, newContent)
            } catch (_: Exception) {
            }
        }
        return replacedCount
    }
}

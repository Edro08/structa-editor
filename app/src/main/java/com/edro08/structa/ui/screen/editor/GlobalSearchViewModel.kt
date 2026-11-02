package com.edro08.structa.ui.screen.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edro08.structa.application.search.SearchWorkspace
import com.edro08.structa.application.search.WorkspaceSearchMatch
import com.edro08.structa.domain.document.DocumentId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GlobalSearchState(
    val visible: Boolean = false,
    val query: String = "",
    val replacement: String = "",
    val caseSensitive: Boolean = false,
    val searching: Boolean = false,
    val replacing: Boolean = false,
    val filesSearched: Int = 0,
    val matches: List<WorkspaceSearchMatch> = emptyList(),
    val message: String? = null
)

class GlobalSearchViewModel(
    private val searchWorkspace: SearchWorkspace
) : ViewModel() {
    private val mutableState = MutableStateFlow(GlobalSearchState())
    val state = mutableState.asStateFlow()
    private var root: DocumentId? = null
    private var searchJob: Job? = null

    fun setRoot(rootId: DocumentId?) {
        root = rootId
    }

    fun open() {
        mutableState.value = state.value.copy(
            visible = true,
            message = if (root == null) "Elige una carpeta de workspace en Explorar." else null
        )
        if (state.value.query.isNotEmpty() && root != null) {
            executeSearch()
        }
    }

    fun close() {
        searchJob?.cancel()
        searchJob = null
        mutableState.value = state.value.copy(visible = false, searching = false)
    }

    fun setQuery(query: String) {
        if (state.value.query == query) return
        mutableState.value = state.value.copy(query = query)
        if (query.length >= 2 && root != null) {
            executeSearch()
        } else if (query.isEmpty()) {
            searchJob?.cancel()
            mutableState.value = state.value.copy(matches = emptyList(), searching = false, message = null)
        }
    }

    fun setReplacement(replacement: String) {
        mutableState.value = state.value.copy(replacement = replacement)
    }

    fun setCaseSensitive(caseSensitive: Boolean) {
        mutableState.value = state.value.copy(caseSensitive = caseSensitive)
        if (state.value.query.isNotEmpty()) executeSearch()
    }

    fun executeSearch() {
        val rootId = root ?: return
        val q = state.value.query
        if (q.isEmpty()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            mutableState.value = state.value.copy(searching = true, message = null)
            val result = searchWorkspace.search(
                root = rootId,
                query = q,
                caseSensitive = state.value.caseSensitive,
                onProgress = { searched, _ ->
                    mutableState.value = state.value.copy(filesSearched = searched)
                }
            )
            mutableState.value = state.value.copy(
                searching = false,
                matches = result.matches,
                filesSearched = result.filesSearched,
                message = if (result.matches.isEmpty()) "No se encontraron coincidencias."
                          else if (result.truncated) "Mostrando ${result.matches.size} coincidencias." else null
            )
        }
    }

    fun replaceAll() {
        val rep = state.value.replacement
        val currentMatches = state.value.matches
        if (currentMatches.isEmpty() || root == null) return

        viewModelScope.launch {
            mutableState.value = state.value.copy(replacing = true)
            val replaced = searchWorkspace.replaceInFiles(currentMatches, rep)
            mutableState.value = state.value.copy(
                replacing = false,
                message = "Reemplazadas $replaced ocurrencias."
            )
            executeSearch()
        }
    }
}

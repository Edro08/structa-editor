package com.edro08.structa.ui.screen.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edro08.structa.R
import com.edro08.structa.application.search.WorkspaceSearchMatch
import com.edro08.structa.domain.editor.navigation.DocumentSymbol

/** Keyboard and touch share selection; Escape dismisses without changing the document. */
@Composable
fun ProductivityPicker(title: String, query: String, onQuery: (String) -> Unit,
    labels: List<Pair<String, String>>, message: String?, loading: Boolean,
    onChoose: (Int) -> Unit, onDismiss: () -> Unit) {
    var selected by remember(query, labels) { mutableIntStateOf(0) }
    val focus = remember { FocusRequester() }
    val scroll = rememberLazyListState()
    LaunchedEffect(selected) { if (selected in labels.indices) scroll.animateScrollToItem(selected) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = {
            LaunchedEffect(Unit) { withFrameNanos { }; focus.requestFocus() }
            Column(Modifier.onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) false else when (event.key) {
                    Key.Escape -> { onDismiss(); true }
                    Key.DirectionDown -> { selected = (selected + 1).coerceAtMost((labels.size - 1).coerceAtLeast(0)); true }
                    Key.DirectionUp -> { selected = (selected - 1).coerceAtLeast(0); true }
                    Key.Enter, Key.NumPadEnter -> { if (selected in labels.indices) onChoose(selected); true }
                    else -> false
                }
            }) {
                 TextField(query, onQuery, label = { Text(stringResource(R.string.editor_filter)) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focus))
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                 if (labels.isEmpty() && !loading) Text(stringResource(R.string.editor_no_results))
                LazyColumn(Modifier.heightIn(max = 320.dp), state = scroll) {
                    itemsIndexed(labels) { index, (label, detail) ->
                        Surface(color = if (index == selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface) {
                            Column(Modifier.fillMaxWidth().clickable { onChoose(index) }.padding(8.dp)) {
                                Text(label)
                                if (detail.isNotEmpty()) Text(detail, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
         }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) } })
}

@Composable
fun DocumentOutlinePicker(
    symbols: List<DocumentSymbol>,
    loading: Boolean = false,
    onSelect: (DocumentSymbol) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, symbols) {
        if (query.isBlank()) symbols
        else symbols.filter {
            it.name.contains(query, ignoreCase = true) || it.detail.contains(query, ignoreCase = true)
        }
    }
    val labels = remember(filtered) {
        filtered.map { sym ->
            sym.name to "${sym.detail.ifEmpty { sym.kind.name.lowercase() }} · línea ${sym.line + 1}"
        }
    }
    ProductivityPicker(
        title = "Índice de contenido",
        query = query,
        onQuery = { query = it },
        labels = labels,
        message = if (!loading && symbols.isEmpty()) "No se detectaron secciones o títulos en este documento." else null,
        loading = loading,
        onChoose = { index ->
            if (index in filtered.indices) {
                onSelect(filtered[index])
                onDismiss()
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
fun GlobalSearchDialog(
    state: GlobalSearchState,
    onQuery: (String) -> Unit,
    onReplacement: (String) -> Unit,
    onCaseSensitive: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onReplaceAll: () -> Unit,
    onSelectMatch: (WorkspaceSearchMatch) -> Unit,
    onDismiss: () -> Unit
) {
    val focus = remember { FocusRequester() }
    val scroll = rememberLazyListState()
    var showReplaceBox by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Buscar en Workspace", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { showReplaceBox = !showReplaceBox }) {
                    Icon(
                        if (showReplaceBox) Icons.Filled.Search else Icons.Filled.FindReplace,
                        contentDescription = "Alternar Reemplazar"
                    )
                }
            }
        },
        text = {
            LaunchedEffect(Unit) {
                withFrameNanos { }
                focus.requestFocus()
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = state.query,
                    onValueChange = onQuery,
                    label = { Text("Buscar en archivos...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                    trailingIcon = {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Filled.Search, contentDescription = "Buscar")
                        }
                    }
                )

                if (showReplaceBox) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = state.replacement,
                            onValueChange = onReplacement,
                            label = { Text("Reemplazar por...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onReplaceAll,
                            enabled = state.matches.isNotEmpty() && !state.replacing
                        ) {
                            Text("Reemplazar")
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCaseSensitive(!state.caseSensitive) }
                    ) {
                        Checkbox(checked = state.caseSensitive, onCheckedChange = onCaseSensitive)
                        Text("Coincidir mayúsculas", style = MaterialTheme.typography.bodySmall)
                    }
                    if (state.filesSearched > 0) {
                        Text(
                            "${state.matches.size} en ${state.filesSearched} arch.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (state.searching || state.replacing) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 4.dp))
                }

                state.message?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp), state = scroll) {
                    itemsIndexed(state.matches) { _, match ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectMatch(match) }
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text(
                                    "${match.file.relativePath} : ${match.line + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    match.lineContent.trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        }
    )
}

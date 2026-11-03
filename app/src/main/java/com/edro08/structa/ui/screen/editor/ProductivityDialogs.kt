package com.edro08.structa.ui.screen.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edro08.structa.R
import com.edro08.structa.application.search.WorkspaceSearchMatch
import com.edro08.structa.domain.editor.navigation.DocumentSymbol
import com.edro08.structa.ui.theme.StructaCyan
import com.edro08.structa.ui.theme.StructaLavender
import com.edro08.structa.ui.theme.StructaPurple

/** Keyboard and touch share selection; Escape dismisses without changing the document. */
@Composable
fun ProductivityPicker(
    title: String,
    query: String,
    onQuery: (String) -> Unit,
    labels: List<Pair<String, String>>,
    message: String?,
    loading: Boolean,
    onChoose: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember(query, labels) { mutableIntStateOf(0) }
    val focus = remember { FocusRequester() }
    val scroll = rememberLazyListState()
    LaunchedEffect(selected) { if (selected in labels.indices) scroll.animateScrollToItem(selected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            RoundedCornerShape(24.dp)
        ),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(StructaPurple.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = StructaPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.common_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            LaunchedEffect(Unit) { withFrameNanos { }; focus.requestFocus() }
            Column(
                Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) false else when (event.key) {
                            Key.Escape -> { onDismiss(); true }
                            Key.DirectionDown -> { selected = (selected + 1).coerceAtMost((labels.size - 1).coerceAtLeast(0)); true }
                            Key.DirectionUp -> { selected = (selected - 1).coerceAtLeast(0); true }
                            Key.Enter, Key.NumPadEnter -> { if (selected in labels.indices) onChoose(selected); true }
                            else -> false
                        }
                    }
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQuery,
                    label = { Text(stringResource(R.string.editor_filter)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedBorderColor = StructaPurple,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus)
                )
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 4.dp))
                message?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
                if (labels.isEmpty() && !loading) {
                    Text(
                        stringResource(R.string.editor_no_results),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 320.dp), state = scroll) {
                    itemsIndexed(labels) { index, (label, detail) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .border(
                                    1.dp,
                                    if (index == selected) StructaPurple.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                    RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            color = if (index == selected) StructaPurple.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f),
                            onClick = { onChoose(index) }
                        ) {
                            Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (index == selected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (detail.isNotEmpty()) {
                                    Text(
                                        detail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text(stringResource(R.string.common_close))
            }
        }
    )
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
        modifier = Modifier.border(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            RoundedCornerShape(24.dp)
        ),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(StructaPurple.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = StructaPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        "Buscar en Workspace",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { showReplaceBox = !showReplaceBox },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (showReplaceBox) StructaPurple.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        if (showReplaceBox) Icons.Filled.Search else Icons.Filled.FindReplace,
                        contentDescription = "Alternar Reemplazar",
                        tint = if (showReplaceBox) StructaLavender else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
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
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQuery,
                    label = { Text("Buscar en archivos...") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedBorderColor = StructaPurple,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                    trailingIcon = {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = StructaPurple)
                        }
                    }
                )

                if (showReplaceBox) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.replacement,
                            onValueChange = onReplacement,
                            label = { Text("Reemplazar por...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                focusedBorderColor = StructaPurple,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onReplaceAll,
                            enabled = state.matches.isNotEmpty() && !state.replacing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StructaPurple,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Reemplazar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (state.caseSensitive) StructaPurple.copy(alpha = 0.15f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (state.caseSensitive) StructaPurple.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        onClick = { onCaseSensitive(!state.caseSensitive) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = state.caseSensitive,
                                onCheckedChange = null,
                                modifier = Modifier.size(24.dp),
                                colors = CheckboxDefaults.colors(checkedColor = StructaPurple)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Coincidir mayúsculas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    if (state.filesSearched > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                "${state.matches.size} en ${state.filesSearched} arch.",
                                style = MaterialTheme.typography.labelSmall,
                                color = StructaLavender,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (state.searching || state.replacing) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 4.dp))
                }

                state.message?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(vertical = 4.dp))
                }

                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp), state = scroll) {
                    itemsIndexed(state.matches) { _, match ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                            onClick = { onSelectMatch(match) }
                        ) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    "${match.file.relativePath} : ${match.line + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StructaCyan
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    match.lineContent.trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
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
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text(stringResource(R.string.common_close))
            }
        }
    )
}

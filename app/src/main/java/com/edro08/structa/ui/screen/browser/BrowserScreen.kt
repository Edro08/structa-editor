package com.edro08.structa.ui.screen.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.edro08.structa.R
import com.edro08.structa.domain.filesystem.FileEntry
import com.edro08.structa.domain.filesystem.FileRef
import com.edro08.structa.domain.filesystem.FileSystem
import com.edro08.structa.ui.component.*
import com.edro08.structa.ui.theme.ScreenStyle
import com.edro08.structa.ui.theme.StructaSpacing
import com.edro08.structa.ui.theme.StructaSizes
import com.edro08.structa.ui.theme.StructaLavender

enum class BrowserMode { EXPLORE, SELECT_DIRECTORY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(state: BrowserUiState, onBack: () -> Unit, onChooseFolder: () -> Unit,
    onQuery: (String) -> Unit, onEntry: (FileEntry) -> Unit, onRetry: () -> Unit,
    onToggle: (FileEntry) -> Unit = {}, onCreate: (String, Boolean) -> Unit = { _, _ -> },
    onRename: (FileEntry, String) -> Unit = { _, _ -> }, onDelete: (FileEntry) -> Unit = {},
    mode: BrowserMode = BrowserMode.EXPLORE, fileSystem: FileSystem? = null,
    root: FileRef? = state.workspace?.root, onBreadcrumb: (Int) -> Unit = {}, onSelectDirectory: () -> Unit = {}) {
    val selecting = mode == BrowserMode.SELECT_DIRECTORY
    var action by remember(mode, fileSystem, root) { mutableStateOf<BrowserAction?>(null) }
    var target by remember { mutableStateOf<FileEntry?>(null) }
    var name by remember { mutableStateOf("") }
    if (action != null) AlertDialog(onDismissRequest = { action = null },
        title = { Text(stringResource(when (action!!) {
            BrowserAction.NEW_FILE -> R.string.browser_new_file
            BrowserAction.NEW_FOLDER -> R.string.browser_new_folder
            BrowserAction.RENAME -> R.string.browser_rename
            BrowserAction.DELETE -> R.string.browser_delete
        })) }, text = {
            if (action == BrowserAction.DELETE) Text(stringResource(R.string.dialog_browser_delete_confirm, target?.name.orEmpty()))
            else TextField(value = name, onValueChange = { name = it }, singleLine = true,
                label = { Text(stringResource(R.string.browser_name)) })
        }, confirmButton = { TextButton(enabled = !state.busy && (action == BrowserAction.DELETE || name.isNotBlank()), onClick = {
            when (action) {
                BrowserAction.DELETE -> target?.let(onDelete)
                BrowserAction.RENAME -> target?.let { onRename(it, name) }
                BrowserAction.NEW_FILE, BrowserAction.NEW_FOLDER -> onCreate(name, action == BrowserAction.NEW_FOLDER)
                null -> Unit
            }
            action = null
        }) { Text(stringResource(R.string.common_confirm)) } }, dismissButton = {
            TextButton(onClick = { action = null }) { Text(stringResource(R.string.common_cancel)) }
        })

    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = { StructaTopBar(title = { Column {
            Text(stringResource(if (selecting) R.string.browser_select_title else R.string.browser_title))
            if (selecting) Text(stringResource(R.string.browser_direct_subtitle), style = MaterialTheme.typography.labelSmall)
        } }, onBack = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            val colors = MaterialTheme.colorScheme
            val searchShape = RoundedCornerShape(14.dp)
            val searchBorder = colors.outlineVariant.copy(alpha = 0.4f)

            // Fila de Breadcrumbs (izquierda) y Acciones rápidas [↻] [+] (derecha)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenStyle.pagePadding, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ruta de navegación (Breadcrumb)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.clickable(
                            enabled = !state.busy,
                            onClick = if (selecting) {{ onBreadcrumb(0) }} else onChooseFolder
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = StructaLavender
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(if (selecting) R.string.browser_internal_storage else R.string.browser_workspace),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = colors.onSurface
                        )
                    }
                    (if (selecting) state.breadcrumbs.drop(1) else state.breadcrumbs).forEachIndexed { index, folder ->
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(14.dp),
                            tint = colors.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = folder,
                            modifier = Modifier.clickable(enabled = selecting) { onBreadcrumb(index + 1) },
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = colors.onSurface
                        )
                    }
                }

                // Acciones a la derecha: [↻] y [+] en lavanda sin contenedor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onRetry,
                        enabled = !state.busy && state.stack.isNotEmpty(),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.browser_refresh),
                            modifier = Modifier.size(24.dp),
                            tint = StructaLavender
                        )
                    }

                    if (state.stack.isNotEmpty()) {
                        var addMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { addMenuExpanded = true },
                                enabled = !state.busy,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = stringResource(R.string.browser_add),
                                    modifier = Modifier.size(26.dp),
                                    tint = StructaLavender
                                )
                            }
                            DropdownMenu(
                                expanded = addMenuExpanded,
                                onDismissRequest = { addMenuExpanded = false }
                            ) {
                                if (!selecting) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.browser_new_file)) },
                                        onClick = {
                                            addMenuExpanded = false
                                            name = ""
                                            action = BrowserAction.NEW_FILE
                                        },
                                        leadingIcon = {
                                            Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.browser_new_folder)) },
                                    onClick = {
                                        addMenuExpanded = false
                                        name = ""
                                        action = BrowserAction.NEW_FOLDER
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.CreateNewFolder, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Barra de búsqueda expandida a todo el ancho
            BasicTextField(
                value = state.query,
                onValueChange = onQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenStyle.pagePadding, vertical = 4.dp)
                    .height(44.dp)
                    .background(colors.surface, searchShape)
                    .border(1.dp, searchBorder, searchShape)
                    .padding(horizontal = 14.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = colors.onSurfaceVariant
                        )
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (state.query.isEmpty()) {
                                Text(
                                    stringResource(R.string.browser_search_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )
            Spacer(Modifier.height(6.dp))
            Box(Modifier.weight(1f).fillMaxWidth()) { when {
                state.stack.isEmpty() -> EmptyScreen(stringResource(R.string.browser_no_folder)) {
                    if (!selecting) Button(onClick = onChooseFolder) { Text(stringResource(R.string.common_choose_folder)) }
                }
                state.loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                state.error != null -> Column(Modifier.padding(StructaSpacing.section)) {
                    Text(when (state.errorRes) {
                        R.string.error_open_documents -> stringResource(R.string.error_open_documents)
                        R.string.error_folder_read_failed -> if (state.error.isEmpty())
                            stringResource(R.string.error_folder_read_failed_generic)
                            else stringResource(R.string.error_folder_read_failed, state.error)
                        else -> if (state.error.isEmpty()) stringResource(R.string.error_operation_failed)
                            else stringResource(R.string.error_operation_failed_detail, state.error)
                    })
                    TextButton(onClick = onRetry) { Text(stringResource(R.string.browser_retry)) }
                    if (!selecting) TextButton(onClick = onChooseFolder) { Text(stringResource(R.string.common_choose_folder)) }
                }
                state.entries.none { !selecting || it.isDirectory } -> EmptyScreen(stringResource(R.string.browser_empty))
                else -> {
                    val rows = buildList<Pair<FileEntry, Int>> {
                        fun addEntries(entries: List<FileEntry>, depth: Int) {
                            entries.filter { !selecting || it.isDirectory }.forEach { entry ->
                                add(entry to depth)
                                if (!selecting && entry.id in state.expanded) addEntries(state.children[entry.id].orEmpty(), depth + 1)
                            }
                        }
                        addEntries(state.entries, 0)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = ScreenStyle.sectionGap)
                    ) {
                        itemsIndexed(rows, key = { _, pair -> pair.first.id.value }) { index, (entry, depth) ->
                            var menu by remember { mutableStateOf(false) }
                            FileRow(
                                entry = entry,
                                onClick = { onEntry(entry) },
                                modifier = Modifier.padding(start = (depth * 14).dp),
                                detail = if (entry.isDirectory) state.children[entry.id]?.let {
                                    pluralStringResource(R.plurals.browser_folder_elements, it.size, it.size)
                                } else null
                            ) {
                                if (!selecting) Box {
                                    IconButton(onClick = { menu = true }, enabled = !state.busy,
                                        modifier = Modifier.size(38.dp)) {
                                        Icon(Icons.Filled.MoreVert,
                                            contentDescription = stringResource(R.string.browser_actions_named, entry.name),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                        DropdownMenuItem(text = { Text(stringResource(R.string.browser_open_named, entry.name)) },
                                            onClick = { menu = false; onEntry(entry) },
                                            leadingIcon = { Icon(Icons.Filled.FolderOpen, contentDescription = null) })
                                        if (entry.isDirectory) DropdownMenuItem(text = { Text(stringResource(
                                            if (entry.id in state.expanded) R.string.browser_collapse_named
                                            else R.string.browser_expand_named, entry.name)) },
                                            onClick = { menu = false; onToggle(entry) },
                                            leadingIcon = { Icon(Icons.Filled.ExpandMore, contentDescription = null) })
                                        DropdownMenuItem(text = { Text(stringResource(R.string.browser_rename_named, entry.name)) },
                                            onClick = { menu = false; target = entry; name = entry.name; action = BrowserAction.RENAME },
                                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) })
                                        DropdownMenuItem(text = { Text(stringResource(R.string.browser_delete_named, entry.name)) },
                                            onClick = { menu = false; target = entry; action = BrowserAction.DELETE },
                                            leadingIcon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null) })
                                    }
                                }
                            }
                            HorizontalDivider(
                                color = colors.outlineVariant.copy(alpha = 0.2f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            } }
            if (selecting) Button(onClick = onSelectDirectory,
                enabled = state.stack.isNotEmpty() && !state.loading && state.error == null && !state.busy,
                modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenStyle.pagePadding, vertical = 12.dp)) {
                Text(stringResource(R.string.browser_use_folder))
            }
        }
    }
}

private enum class BrowserAction { NEW_FILE, NEW_FOLDER, RENAME, DELETE }

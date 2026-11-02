package com.edro08.structa.ui.screen.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.edro08.structa.ui.theme.StructaPurple
import com.edro08.structa.ui.theme.structaColors

enum class BrowserMode { EXPLORE, SELECT_DIRECTORY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    state: BrowserUiState,
    onBack: () -> Unit,
    onChooseFolder: () -> Unit,
    onQuery: (String) -> Unit,
    onEntry: (FileEntry) -> Unit,
    onRetry: () -> Unit,
    onToggle: (FileEntry) -> Unit = {},
    onCreate: (String, Boolean) -> Unit = { _, _ -> },
    onRename: (FileEntry, String) -> Unit = { _, _ -> },
    onDelete: (FileEntry) -> Unit = {},
    mode: BrowserMode = BrowserMode.EXPLORE,
    fileSystem: FileSystem? = null,
    root: FileRef? = state.workspace?.root,
    onBreadcrumb: (Int) -> Unit = {},
    onSelectDirectory: () -> Unit = {}
) {
    val selecting = mode == BrowserMode.SELECT_DIRECTORY
    var action by remember(mode, fileSystem, root) { mutableStateOf<BrowserAction?>(null) }
    var target by remember { mutableStateOf<FileEntry?>(null) }
    var name by remember { mutableStateOf("") }

    if (action != null) {
        AlertDialog(
            onDismissRequest = { action = null },
            title = {
                Text(stringResource(when (action!!) {
                    BrowserAction.NEW_FILE -> R.string.browser_new_file
                    BrowserAction.NEW_FOLDER -> R.string.browser_new_folder
                    BrowserAction.RENAME -> R.string.browser_rename
                    BrowserAction.DELETE -> R.string.browser_delete
                }))
            },
            text = {
                if (action == BrowserAction.DELETE) {
                    Text(stringResource(R.string.dialog_browser_delete_confirm, target?.name.orEmpty()))
                } else {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.browser_name)) }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !state.busy && (action == BrowserAction.DELETE || name.isNotBlank()),
                    onClick = {
                        when (action) {
                            BrowserAction.DELETE -> target?.let(onDelete)
                            BrowserAction.RENAME -> target?.let { onRename(it, name) }
                            BrowserAction.NEW_FILE, BrowserAction.NEW_FOLDER -> onCreate(name, action == BrowserAction.NEW_FOLDER)
                            null -> Unit
                        }
                        action = null
                    }
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { action = null }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StructaTopBar(
                title = {
                    Column {
                        Text(stringResource(if (selecting) R.string.browser_select_title else R.string.browser_title))
                        if (selecting) {
                            Text(stringResource(R.string.browser_direct_subtitle), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                onBack = onBack
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            val colors = MaterialTheme.colorScheme
            val searchShape = RoundedCornerShape(14.dp)
            val searchBorder = colors.outlineVariant.copy(alpha = 0.4f)

            when {
                // Caso 1: Ninguna carpeta abierta
                state.stack.isEmpty() -> {
                    BrowserEmptyWorkspaceView(onChooseFolder = onChooseFolder)
                }

                // Caso 2: Error al acceder a la carpeta y la lista está vacía
                state.error != null && state.entries.isEmpty() -> {
                    BrowserErrorView(
                        error = state.error,
                        errorRes = state.errorRes,
                        onRetry = onRetry,
                        onChooseFolder = onChooseFolder,
                        selecting = selecting
                    )
                }

                // Caso 3: Workspace abierto
                else -> {
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

                    if (state.loading) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }

                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        if (state.entries.isEmpty() && !state.loading) {
                            BrowserEmptyFolderView()
                        } else {
                            val rows = buildList<Pair<FileEntry, Int>> {
                                fun addEntries(entries: List<FileEntry>, depth: Int) {
                                    entries.filter { !selecting || it.isDirectory }.forEach { entry ->
                                        add(entry to depth)
                                        if (!selecting && entry.id in state.expanded) {
                                            addEntries(state.children[entry.id].orEmpty(), depth + 1)
                                        }
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
                                            IconButton(
                                                onClick = { menu = true },
                                                enabled = !state.busy,
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.MoreVert,
                                                    contentDescription = stringResource(R.string.browser_actions_named, entry.name),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                                DropdownMenuItem(
                                                    text = { Text(stringResource(R.string.browser_open_named, entry.name)) },
                                                    onClick = { menu = false; onEntry(entry) },
                                                    leadingIcon = { Icon(Icons.Filled.FolderOpen, contentDescription = null) }
                                                )
                                                if (entry.isDirectory) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(stringResource(
                                                                if (entry.id in state.expanded) R.string.browser_collapse_named
                                                                else R.string.browser_expand_named, entry.name
                                                            ))
                                                        },
                                                        onClick = { menu = false; onToggle(entry) },
                                                        leadingIcon = { Icon(Icons.Filled.ExpandMore, contentDescription = null) }
                                                    )
                                                }
                                                DropdownMenuItem(
                                                    text = { Text(stringResource(R.string.browser_rename_named, entry.name)) },
                                                    onClick = { menu = false; target = entry; name = entry.name; action = BrowserAction.RENAME },
                                                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text(stringResource(R.string.browser_delete_named, entry.name)) },
                                                    onClick = { menu = false; target = entry; action = BrowserAction.DELETE },
                                                    leadingIcon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null) }
                                                )
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
                    }
                }
            }

            if (selecting) {
                Button(
                    onClick = onSelectDirectory,
                    enabled = state.stack.isNotEmpty() && !state.loading && state.error == null && !state.busy,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenStyle.pagePadding, vertical = 12.dp)
                ) {
                    Text(stringResource(R.string.browser_use_folder))
                }
            }
        }
    }
}

@Composable
private fun BrowserEmptyWorkspaceView(onChooseFolder: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(StructaPurple.copy(alpha = 0.12f))
                    .border(1.dp, StructaPurple.copy(alpha = 0.25f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FolderOpen,
                    contentDescription = null,
                    tint = StructaLavender,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.browser_no_workspace_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.browser_no_workspace_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onChooseFolder,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StructaPurple,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_open_workspace),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun BrowserErrorView(
    error: String?,
    errorRes: Int?,
    onRetry: () -> Unit,
    onChooseFolder: () -> Unit,
    selecting: Boolean
) {
    val colors = MaterialTheme.colorScheme
    val errorMessage = when (errorRes) {
        R.string.error_open_documents -> stringResource(R.string.error_open_documents)
        R.string.error_folder_read_failed -> if (error.isNullOrEmpty())
            stringResource(R.string.error_folder_read_failed_generic)
            else stringResource(R.string.error_folder_read_failed, error)
        else -> if (error.isNullOrEmpty()) stringResource(R.string.error_operation_failed)
            else stringResource(R.string.error_operation_failed_detail, error)
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.structaColors.statusWarning.copy(alpha = 0.12f))
                    .border(1.dp, MaterialTheme.structaColors.statusWarning.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.structaColors.statusWarning,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.browser_folder_error_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!selecting) {
                    Button(
                        onClick = onChooseFolder,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StructaPurple, contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.common_choose_folder), fontWeight = FontWeight.Medium)
                    }
                }
                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.outlineVariant)
                ) {
                    Text(stringResource(R.string.browser_retry), color = colors.onSurface)
                }
            }
        }
    }
}

@Composable
private fun BrowserEmptyFolderView() {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.browser_folder_empty_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.browser_folder_empty_desc),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private enum class BrowserAction { NEW_FILE, NEW_FOLDER, RENAME, DELETE }

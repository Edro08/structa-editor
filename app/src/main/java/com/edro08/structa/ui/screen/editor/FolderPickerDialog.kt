package com.edro08.structa.ui.screen.editor

import android.os.Environment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.edro08.structa.R
import com.edro08.structa.ui.theme.DarkOutlineVariant
import com.edro08.structa.ui.theme.DarkSurface
import com.edro08.structa.ui.theme.DarkSurfaceRaised
import com.edro08.structa.ui.theme.DarkTextPrimary
import com.edro08.structa.ui.theme.DarkTextSecondary
import com.edro08.structa.ui.theme.StatusOrange
import com.edro08.structa.ui.theme.StatusRed
import com.edro08.structa.ui.theme.StructaCyan
import com.edro08.structa.ui.theme.StructaLavender
import com.edro08.structa.ui.theme.StructaPurple
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class StorageVolume(
    val name: String,
    val root: File,
    val icon: ImageVector
)

/**
 * Pure OLED directory picker dialog.
 * Enables visual navigation, storage volume switching (Internal / SD Card),
 * and directory creation when choosing destination paths for "Guardar como...".
 */
@Composable
fun FolderPickerDialog(
    initialPath: String,
    onSelectFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val storageRoot = remember { Environment.getExternalStorageDirectory() }
    val initialFile = remember(initialPath) {
        val f = File(initialPath)
        if (f.isDirectory && f.exists()) f
        else f.parentFile?.takeIf { it.isDirectory && it.exists() } ?: storageRoot
    }

    // Detect actual storage devices (internal storage + physical SD cards / USB storage)
    val storageVolumes = remember {
        val list = mutableListOf<StorageVolume>()
        list.add(StorageVolume("Almacenamiento interno", storageRoot, Icons.Filled.PhoneAndroid))
        try {
            val storageParent = File("/storage")
            val secondaries = storageParent.listFiles()?.filter {
                it.isDirectory && it.canRead() &&
                    it.name != "emulated" && it.name != "self" &&
                    !it.name.startsWith(".")
            } ?: emptyList()
            for (sec in secondaries) {
                val label = if (secondaries.size == 1) "Tarjeta SD" else "Tarjeta SD (${sec.name})"
                list.add(StorageVolume(label, sec, Icons.Filled.SdCard))
            }
        } catch (_: Exception) {}
        list
    }

    var currentFolder by remember { mutableStateOf(initialFile) }
    var subfolders by remember { mutableStateOf<List<File>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var createFolderError by remember { mutableStateOf<String?>(null) }

    // Load subdirectories whenever currentFolder changes
    LaunchedEffect(currentFolder) {
        loading = true
        searchQuery = ""
        try {
            val list = withContext(Dispatchers.IO) {
                currentFolder.listFiles()
                    ?.filter { file ->
                        file.isDirectory && !file.name.startsWith(".") && file.canRead() &&
                            !file.name.equals("data", ignoreCase = true) &&
                            !file.name.equals("obb", ignoreCase = true)
                    }
                    ?.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                    ?: emptyList()
            }
            subfolders = list
        } catch (_: Exception) {
            subfolders = emptyList()
        } finally {
            loading = false
        }
    }

    val canGoUp = remember(currentFolder) {
        val parent = currentFolder.parentFile
        parent != null && parent.canRead() && currentFolder.absolutePath != "/"
    }

    val filteredFolders = remember(subfolders, searchQuery) {
        if (searchQuery.isBlank()) subfolders
        else subfolders.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.82f)
                .border(1.dp, DarkOutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with Badge & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(StructaPurple.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderOpen,
                                contentDescription = null,
                                tint = StructaLavender,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Seleccionar carpeta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "Elige la ubicación de destino",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = DarkTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Storage Volume Chips (Internal Storage, SD Card if mounted)
                if (storageVolumes.size > 1 || currentFolder.absolutePath != storageRoot.absolutePath) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        storageVolumes.forEach { volume ->
                            val isSelected = currentFolder.absolutePath == volume.root.absolutePath ||
                                currentFolder.absolutePath.startsWith(volume.root.absolutePath + File.separator)
                            LocationChip(
                                icon = volume.icon,
                                label = volume.name,
                                selected = isSelected,
                                onClick = { currentFolder = volume.root }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // Breadcrumb / Navigation Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceRaised,
                    border = BorderStroke(1.dp, DarkOutlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { currentFolder.parentFile?.let { currentFolder = it } },
                            enabled = canGoUp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Subir nivel",
                                tint = if (canGoUp) StructaLavender else DarkTextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = currentFolder.absolutePath,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                newFolderName = ""
                                createFolderError = null
                                showCreateFolderDialog = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CreateNewFolder,
                                contentDescription = "Nueva carpeta",
                                tint = StructaCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Filter subfolders by name if directory has many folders
                if (subfolders.size > 5) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filtrar carpetas...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = DarkTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Limpiar",
                                        tint = DarkTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceRaised,
                            unfocusedContainerColor = DarkSurfaceRaised,
                            focusedBorderColor = StructaPurple,
                            unfocusedBorderColor = DarkOutlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Folder List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceRaised.copy(alpha = 0.5f))
                        .border(1.dp, DarkOutlineVariant.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                ) {
                    when {
                        loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = StructaPurple, modifier = Modifier.size(28.dp))
                            }
                        }
                        filteredFolders.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Sin coincidencias" else "No hay subcarpetas aquí",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkTextSecondary
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(filteredFolders, key = { it.absolutePath }) { folder ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { currentFolder = folder }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Folder,
                                            contentDescription = null,
                                            tint = StructaLavender,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = folder.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = DarkTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = DarkTextSecondary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkOutlineVariant.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextSecondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.common_cancel))
                    }

                    Button(
                        onClick = { onSelectFolder(currentFolder.canonicalPath) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StructaPurple,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Usar esta carpeta", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Modal to create a new folder
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            modifier = Modifier.border(
                1.dp,
                DarkOutlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(24.dp)
            ),
            shape = RoundedCornerShape(24.dp),
            containerColor = DarkSurface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(StructaCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CreateNewFolder,
                        contentDescription = null,
                        tint = StructaCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Nueva carpeta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = {
                        newFolderName = it
                        createFolderError = null
                    },
                    label = { Text("Nombre de la carpeta") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    isError = createFolderError != null,
                    supportingText = createFolderError?.let { { Text(it, color = StatusRed) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceRaised,
                        unfocusedContainerColor = DarkSurfaceRaised,
                        focusedBorderColor = StructaCyan,
                        unfocusedBorderColor = DarkOutlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFolderName.trim()
                        if (name.isEmpty() || name.contains('/') || name.contains('\\')) {
                            createFolderError = "Nombre no válido"
                        } else {
                            val target = File(currentFolder, name)
                            if (target.exists()) {
                                createFolderError = "La carpeta ya existe"
                            } else if (target.mkdirs()) {
                                showCreateFolderDialog = false
                                currentFolder = target
                            } else {
                                createFolderError = "No se pudo crear la carpeta"
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StructaCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Crear", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCreateFolderDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextSecondary)
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun LocationChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) StructaPurple.copy(alpha = 0.2f) else DarkSurfaceRaised,
        border = BorderStroke(
            1.dp,
            if (selected) StructaPurple.copy(alpha = 0.8f) else DarkOutlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) StructaPurple else DarkTextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) StructaLavender else DarkTextPrimary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

package com.edro08.structa.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edro08.structa.R
import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.workspace.WorkspaceShortcut
import com.edro08.structa.ui.component.WorkspaceBadge
import com.edro08.structa.ui.component.WorkspacePrimaryButton
import com.edro08.structa.ui.component.WorkspaceSectionTitle
import com.edro08.structa.ui.theme.ScreenStyle
import com.edro08.structa.ui.theme.StatusOrange
import java.util.Calendar

@Composable
fun HomeScreen(state: HomeUiState, onOpenWorkspace: (DocumentId) -> Unit, onChooseFolder: () -> Unit,
    onToggleFavorite: (DocumentId) -> Unit, onRemoveFromHistory: (DocumentId) -> Unit) {
    var expandedFavorites by rememberSaveable { mutableStateOf(false) }
    var expandedRecent by rememberSaveable { mutableStateOf(false) }
    var pendingRemoval by remember { mutableStateOf<WorkspaceShortcut?>(null) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(
            start = ScreenStyle.pagePadding, end = ScreenStyle.pagePadding, top = 12.dp,
            bottom = ScreenStyle.sectionGap), verticalArrangement = Arrangement.spacedBy(ScreenStyle.sectionGap)) {
            item {
                Column(Modifier.padding(start = 2.dp, top = 4.dp, bottom = 4.dp)) {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                HomeSection(stringResource(R.string.home_favorites), stringResource(R.string.home_no_favorites), true,
                    state.favorites, expandedFavorites, { expandedFavorites = !expandedFavorites }, onOpenWorkspace,
                    onToggleFavorite, { pendingRemoval = it })
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    thickness = 0.5.dp
                )
            }
            item {
                HomeSection(stringResource(R.string.home_recent), stringResource(R.string.home_recent_hint), false,
                    state.recent, expandedRecent, { expandedRecent = !expandedRecent }, onOpenWorkspace,
                    onToggleFavorite, { pendingRemoval = it })
            }
        }
        WorkspacePrimaryButton(stringResource(R.string.home_open_workspace), Icons.Filled.FolderOpen,
            onChooseFolder, Modifier.fillMaxWidth().padding(
                start = ScreenStyle.pagePadding, end = ScreenStyle.pagePadding, bottom = 10.dp))
    }

    pendingRemoval?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            modifier = Modifier.border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(24.dp)
            ),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(StatusOrange.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = StatusOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.home_remove_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.home_remove_description, entry.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { onRemoveFromHistory(entry.id); pendingRemoval = null },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusOrange,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = stringResource(R.string.home_remove_confirm),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingRemoval = null },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun HomeSection(title: String, description: String, favorites: Boolean,
    entries: List<WorkspaceShortcut>, expanded: Boolean, onToggleExpanded: () -> Unit,
    onOpen: (DocumentId) -> Unit, onFavorite: (DocumentId) -> Unit, onRemove: (WorkspaceShortcut) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        WorkspaceSectionTitle(if (favorites) Icons.Filled.Star else Icons.Filled.History, title, description,
            if (entries.size > 3) {{
                TextButton(onClick = onToggleExpanded) {
                    Text(stringResource(if (expanded) R.string.home_show_less else R.string.home_show_all))
                }
            }} else null)
        Spacer(Modifier.height(8.dp))
        val shown = if (expanded) entries else entries.take(3)
        if (shown.isEmpty()) {
            Text(stringResource(if (favorites) R.string.home_empty_favorites else R.string.home_no_recent),
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        shown.forEachIndexed { index, entry ->
            if (index != 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    thickness = 0.5.dp
                )
            }
            WorkspaceRow(entry, !favorites, onOpen, onFavorite) { onRemove(entry) }
        }
    }
}

@Composable
private fun WorkspaceRow(entry: WorkspaceShortcut, recent: Boolean, onOpen: (DocumentId) -> Unit,
    onFavorite: (DocumentId) -> Unit, onRemove: () -> Unit) {
    val openDescription = stringResource(R.string.home_open_named_workspace, entry.name)
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClickLabel = openDescription) { onOpen(entry.id) }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WorkspaceBadge()
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1,
                overflow = TextOverflow.Ellipsis, color = colors.onSurface)
            val path = entry.path?.let { stringResource(R.string.home_workspace_path, it) }
            val subtitle = if (recent) {
                val opened = openedLabel(entry.lastOpenedAt)
                if (path == null) opened else stringResource(R.string.home_recent_detail, opened, path)
            } else path
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = colors.onSurfaceVariant)
        }
        IconButton(onClick = { onFavorite(entry.id) }, modifier = Modifier.size(36.dp)) {
            Icon(if (entry.favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = stringResource(if (entry.favorite) R.string.home_unfavorite else R.string.home_favorite),
                tint = if (entry.favorite) StatusOrange else colors.onSurfaceVariant)
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.home_remove_title),
                tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun openedLabel(time: Long): String {
    val today = Calendar.getInstance()
    val startToday = (today.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val startYesterday = (startToday.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    val days = ((startToday.timeInMillis - time) / (24 * 60 * 60 * 1000L)).toInt() + 1
    return when {
        time >= startToday.timeInMillis -> stringResource(R.string.home_opened_today)
        time >= startYesterday.timeInMillis -> stringResource(R.string.home_opened_yesterday)
        else -> pluralStringResource(R.plurals.home_opened_days_ago, days, days)
    }
}

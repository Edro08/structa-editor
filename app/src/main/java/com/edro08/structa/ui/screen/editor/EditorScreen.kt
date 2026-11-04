package com.edro08.structa.ui.screen.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.edro08.structa.R
import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.editor.encoding.DocumentCharset
import com.edro08.structa.domain.editor.command.*
import com.edro08.structa.domain.editor.cursor.TextOffset
import com.edro08.structa.domain.editor.cursor.TextRange
import com.edro08.structa.domain.editor.markdown.MarkdownParser
import com.edro08.structa.ui.screen.editor.markdown.MarkdownViewer
import androidx.compose.material.icons.filled.Description
import com.edro08.structa.domain.editor.navigation.DocumentSymbol
import com.edro08.structa.domain.editor.navigation.DocumentSymbolExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.edro08.structa.domain.editor.syntax.Language
import com.edro08.structa.domain.editor.syntax.LanguageRegistry
import com.edro08.structa.ui.editor.component.StructaEditor
import com.edro08.structa.ui.editor.model.WordWrapMode
import com.edro08.structa.ui.editor.input.EditorAction
import com.edro08.structa.ui.editor.input.applicationActionFor
import com.edro08.structa.domain.editor.search.SearchOptions
import com.edro08.structa.ui.component.formatBytes
import com.edro08.structa.ui.component.EmptyScreen
import com.edro08.structa.ui.component.StructaTopBar
import com.edro08.structa.ui.component.WorkspaceSelection
import com.edro08.structa.ui.theme.ScreenStyle
import com.edro08.structa.ui.theme.StructaLavender
import com.edro08.structa.ui.theme.StructaPurple
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.edro08.structa.ui.theme.DarkTextSecondary
import com.edro08.structa.ui.theme.StatusOrange
import com.edro08.structa.ui.theme.StatusRed
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.WarningAmber
import com.edro08.structa.ui.theme.StructaCyan
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
private fun languageName(language: Language): String = when (language.id) {
    "go" -> stringResource(R.string.editor_language_go)
    "json" -> stringResource(R.string.editor_language_json)
    "yaml" -> stringResource(R.string.editor_language_yaml)
    "markdown" -> stringResource(R.string.editor_language_markdown)
    "xml" -> stringResource(R.string.editor_language_xml)
    "text" -> stringResource(R.string.editor_language_plain)
    else -> language.title
}

@Composable
private fun actionName(action: EditorAction): String = when (action) {
    EditorAction.SAVE -> stringResource(R.string.editor_save)
    EditorAction.FIND -> stringResource(R.string.editor_find)
    EditorAction.REPLACE -> stringResource(R.string.editor_replace)
    EditorAction.GO_TO_LINE -> stringResource(R.string.editor_go_to_line)
    EditorAction.QUICK_OPEN -> stringResource(R.string.editor_open)
    EditorAction.COMMAND_PALETTE -> stringResource(R.string.editor_edit_menu)
    EditorAction.FIND_NEXT -> stringResource(R.string.editor_next)
    EditorAction.FIND_PREVIOUS -> stringResource(R.string.editor_previous)
    EditorAction.UNDO -> stringResource(R.string.editor_undo)
    EditorAction.REDO -> stringResource(R.string.editor_redo)
    EditorAction.FORMAT -> stringResource(R.string.editor_format_document)
    EditorAction.CLOSE -> stringResource(R.string.editor_close)
    EditorAction.TOGGLE_EDITABLE -> stringResource(R.string.editor_toggle_editable)
    EditorAction.DUPLICATE_LINE -> action.title
    EditorAction.DELETE_LINE -> action.title
    EditorAction.DOCUMENT_OUTLINE -> action.title
    EditorAction.GLOBAL_SEARCH -> action.title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(state: EditorUiState, onBack: () -> Unit, onExplore: () -> Unit,
    onLanguage: (Language) -> Unit, onCharset: (DocumentCharset) -> Unit = {}, onAutoDetectCharset: () -> Unit = {}, onSearch: (String) -> Unit,
    onFormat: () -> Unit, onUndo: () -> Unit, onRedo: () -> Unit, onLine: (Int) -> Unit, onSaveAs: (DocumentId) -> Unit = {},
    onMessage: (String) -> Unit,
    onSave: () -> Unit, onSelectDocument: (DocumentId) -> Unit, onCloseDocument: (DocumentId) -> Unit,
    onCancelClose: () -> Unit, onDiscardClose: () -> Unit, onSaveClose: () -> Unit,
    onSearchOptions: (SearchOptions) -> Unit = {}, onReplacement: (String) -> Unit = {},
    onFindNext: (Boolean) -> Unit = {}, onReplace: (Boolean) -> Unit = {}, onQuickOpen: () -> Unit = {},
    editorFont: com.edro08.structa.domain.settings.EditorFont = com.edro08.structa.domain.settings.EditorFont.MONOSPACE,
    editorFontSize: Int = 14, showAccessoryBar: Boolean = true,
    onCloseAll: () -> Unit = {}, onCloseOthers: () -> Unit = {},
    onGoToStart: () -> Unit = {}, onGoToEnd: () -> Unit = {}, onSaveAll: () -> Unit = {},
    onGlobalSearch: () -> Unit = {},
    onToggleEditable: () -> Unit = {},
    onRestoreDraft: () -> Unit = {},
    onDiscardDraft: () -> Unit = {}) {
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showReplace by rememberSaveable { mutableStateOf(false) }
    var showFileMenu by remember { mutableStateOf(false) }
    var showEditMenu by remember { mutableStateOf(false) }
    var showFormatMenu by remember { mutableStateOf(false) }
    var showEncodingMenu by remember { mutableStateOf(false) }
    var showGoToLine by rememberSaveable { mutableStateOf(false) }
    var showSaveAsDialog by rememberSaveable { mutableStateOf(false) }
    var showOutline by rememberSaveable { mutableStateOf(false) }
    var requestedLine by rememberSaveable { mutableStateOf("") }
    val entry = state.entry
    val isMarkdown = remember(entry?.name) {
        entry?.name?.let { it.endsWith(".md", ignoreCase = true) || it.endsWith(".markdown", ignoreCase = true) } == true
    }
    var renderMarkdown by rememberSaveable(entry?.id) { mutableStateOf(true) }
    val searchFocus = remember { FocusRequester() }
    val lineFocus = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(showSearch, showReplace) { if (showSearch && entry != null && !state.loading) searchFocus.requestFocus() }
    LaunchedEffect(state.isEditable, entry?.id) {
        if (!state.isEditable) {
            keyboardController?.hide()
        }
    }
    fun enabled(action: EditorAction): Boolean = when (action) {
        EditorAction.QUICK_OPEN, EditorAction.COMMAND_PALETTE, EditorAction.GLOBAL_SEARCH, EditorAction.TOGGLE_EDITABLE -> true
        EditorAction.SAVE -> entry != null && !state.loading && !state.saving && state.pendingSave == null && state.isEditable
        EditorAction.REPLACE -> entry != null && !state.loading && state.isEditable
        EditorAction.UNDO -> !state.loading && state.canUndo && state.isEditable
        EditorAction.REDO -> !state.loading && state.canRedo && state.isEditable
        EditorAction.FORMAT -> entry != null && !state.loading && !state.formatting && state.isEditable &&
            state.highlightLanguage().id in setOf("json", "xml", "yaml")
        EditorAction.FIND_NEXT, EditorAction.FIND_PREVIOUS -> !state.loading && !state.searching && !state.replacing && state.occurrences > 0
        EditorAction.DUPLICATE_LINE, EditorAction.DELETE_LINE -> entry != null && !state.loading && state.isEditable
        else -> entry != null && !state.loading
    }
    fun dispatch(action: EditorAction) {
        if (!enabled(action)) return
        state.inputSession?.finishComposingText()
        when (action) {
            EditorAction.SAVE -> onSave()
            EditorAction.FIND -> { showReplace = false; showSearch = true }
            EditorAction.REPLACE -> { showReplace = true; showSearch = true }
            EditorAction.GO_TO_LINE -> { requestedLine = ""; showGoToLine = true }
            EditorAction.QUICK_OPEN -> onQuickOpen()
            EditorAction.COMMAND_PALETTE -> { showFileMenu = false; showEditMenu = true }
            EditorAction.FIND_NEXT -> onFindNext(false)
            EditorAction.FIND_PREVIOUS -> onFindNext(true)
            EditorAction.UNDO -> onUndo()
            EditorAction.REDO -> onRedo()
            EditorAction.FORMAT -> onFormat()
            EditorAction.CLOSE -> entry?.let { onCloseDocument(it.id) }
            EditorAction.TOGGLE_EDITABLE -> {
                if (state.isEditable) keyboardController?.hide()
                state.inputSession?.finishComposingText()
                onToggleEditable()
            }
            EditorAction.DUPLICATE_LINE -> state.inputSession?.execute(DuplicateLineCommand(down = true))
            EditorAction.DELETE_LINE -> state.inputSession?.execute(DeleteLineCommand)
            EditorAction.DOCUMENT_OUTLINE -> showOutline = true
            EditorAction.GLOBAL_SEARCH -> onGlobalSearch()
        }
    }
    Scaffold(modifier = Modifier.onPreviewKeyEvent { event ->
        applicationActionFor(event.nativeKeyEvent)?.let { dispatch(it); true } ?: false
    }, topBar = {
        StructaTopBar(title = {
            Column {
                 val title = entry?.name ?: stringResource(R.string.editor_title)
                  Text(if (state.tabs.size == 1 && state.dirty) stringResource(R.string.editor_dirty_title, title) else title,
                      maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (entry != null) {
                    val modeLabel = if (state.isEditable) stringResource(R.string.editor_mode_edit)
                        else if (isMarkdown && renderMarkdown) stringResource(R.string.editor_mode_rendered)
                        else stringResource(R.string.editor_mode_view)
                    Text(stringResource(R.string.editor_document_info, languageName(state.highlightLanguage()), formatBytes(entry.sizeBytes)) + " · " + state.charset.displayName + " · " + modeLabel, maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }, onBack = onBack,
            actions = {
                if (entry != null) {
                    if (isMarkdown && !state.isEditable) {
                        IconButton(
                            onClick = { renderMarkdown = !renderMarkdown }
                        ) {
                            Icon(
                                imageVector = if (renderMarkdown) Icons.Filled.Code else Icons.Filled.Description,
                                contentDescription = if (renderMarkdown) stringResource(R.string.editor_action_view_raw) else stringResource(R.string.editor_action_view_rendered),
                                tint = if (renderMarkdown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (state.isEditable) keyboardController?.hide()
                            state.inputSession?.finishComposingText()
                            onToggleEditable()
                        }
                    ) {
                        Icon(
                            imageVector = if (state.isEditable) Icons.Filled.Check else Icons.Filled.Edit,
                            contentDescription = if (state.isEditable) stringResource(R.string.editor_action_view) else stringResource(R.string.editor_action_edit),
                            tint = if (state.isEditable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box {
                    IconButton(onClick = { showEditMenu = false; showFileMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = stringResource(R.string.editor_file_menu),
                            tint = if (showFileMenu) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(expanded = showFileMenu, onDismissRequest = { showFileMenu = false },
                         modifier = Modifier.widthIn(min = 220.dp).border(1.dp,
                             MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                         shape = RoundedCornerShape(18.dp),
                         containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                         EditorMenuOption(stringResource(R.string.editor_open), Icons.Filled.FolderOpen) {
                             showFileMenu = false; dispatch(EditorAction.QUICK_OPEN)
                         }
                         EditorMenuOption(stringResource(R.string.editor_save), Icons.Filled.Save,
                             enabled = enabled(EditorAction.SAVE)) { showFileMenu = false; dispatch(EditorAction.SAVE) }
                          EditorMenuOption(stringResource(R.string.editor_save_as), Icons.Filled.SaveAs,
                              enabled = state.tabs.isNotEmpty() && !state.saving && state.pendingSave == null) {
                              showFileMenu = false; state.inputSession?.finishComposingText(); showSaveAsDialog = true
                          }
                          EditorMenuOption(stringResource(R.string.editor_save_all), Icons.Filled.Save,
                              enabled = state.tabs.any { it.dirty } && !state.loading && !state.saving &&
                                  state.pendingSave == null && state.pendingClose == null) {
                              showFileMenu = false; onSaveAll()
                          }
                          HorizontalDivider()
                          EditorMenuOption(stringResource(R.string.editor_close), Icons.Filled.Close,
                              enabled = enabled(EditorAction.CLOSE)) { showFileMenu = false; dispatch(EditorAction.CLOSE) }
                          EditorMenuOption(stringResource(R.string.editor_close_all), Icons.Filled.ClearAll,
                              enabled = state.tabs.isNotEmpty() && !state.loading && state.pendingClose == null && !state.saving) {
                              showFileMenu = false; onCloseAll()
                          }
                          EditorMenuOption(stringResource(R.string.editor_close_others), Icons.Filled.Close,
                              enabled = state.tabs.size > 1 && entry != null && !state.loading && state.pendingClose == null && !state.saving) {
                              showFileMenu = false; onCloseOthers()
                          }
                     }
                 }
                 Box {
                     IconButton(onClick = { dispatch(EditorAction.COMMAND_PALETTE) }) {
                         Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.editor_edit_menu_description))
                     }
                     DropdownMenu(expanded = showEditMenu, onDismissRequest = { showEditMenu = false },
                         modifier = Modifier.widthIn(min = 260.dp).border(1.dp,
                             MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                         shape = RoundedCornerShape(18.dp),
                         containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                          listOf(Triple(EditorAction.FIND, Icons.Filled.Search, R.string.editor_shortcut_find),
                              Triple(EditorAction.REPLACE, Icons.Filled.FindReplace, R.string.editor_shortcut_replace),
                              Triple(EditorAction.GO_TO_LINE, Icons.Filled.FormatListNumbered, R.string.editor_shortcut_line))
                              .forEach { (action, icon, shortcut) ->
                                  EditorMenuOption(actionName(action), icon, stringResource(shortcut), enabled(action)) {
                                      showEditMenu = false; dispatch(action)
                                  }
                              }
                          EditorMenuOption(actionName(EditorAction.GLOBAL_SEARCH), Icons.Filled.Search, "Ctrl+Shift+F", enabled(EditorAction.GLOBAL_SEARCH)) {
                              showEditMenu = false; dispatch(EditorAction.GLOBAL_SEARCH)
                          }
                          EditorMenuOption(actionName(EditorAction.DOCUMENT_OUTLINE), Icons.Filled.FormatListNumbered, "Ctrl+Shift+O", enabled(EditorAction.DOCUMENT_OUTLINE)) {
                              showEditMenu = false; dispatch(EditorAction.DOCUMENT_OUTLINE)
                          }

                          EditorMenuOption(stringResource(R.string.editor_go_to_start), Icons.Filled.ArrowUpward,
                              enabled = enabled(EditorAction.GO_TO_LINE)) {
                              showEditMenu = false; state.inputSession?.finishComposingText(); onGoToStart()
                          }
                          EditorMenuOption(stringResource(R.string.editor_go_to_end), Icons.Filled.ArrowDownward,
                              enabled = enabled(EditorAction.GO_TO_LINE)) {
                              showEditMenu = false; state.inputSession?.finishComposingText(); onGoToEnd()
                          }
                          HorizontalDivider()
                         listOf(Triple(EditorAction.UNDO, Icons.AutoMirrored.Filled.Undo, R.string.editor_shortcut_undo),
                             Triple(EditorAction.REDO, Icons.AutoMirrored.Filled.Redo, R.string.editor_shortcut_redo))
                             .forEach { (action, icon, shortcut) ->
                                 EditorMenuOption(actionName(action), icon, stringResource(shortcut), enabled(action)) {
                                     showEditMenu = false; dispatch(action)
                                 }
                             }
                         HorizontalDivider()
                         EditorMenuOption(actionName(EditorAction.FORMAT), Icons.Filled.AutoFixHigh,
                             enabled = enabled(EditorAction.FORMAT)) { showEditMenu = false; dispatch(EditorAction.FORMAT) }
                          EditorMenuOption(stringResource(R.string.editor_format), Icons.Filled.Code,
                              enabled = entry != null && !state.loading, submenu = true) {
                              showEditMenu = false; showFormatMenu = true
                          }
                          EditorMenuOption("Codificación", Icons.Filled.Translate,
                              enabled = entry != null && !state.loading, submenu = true) {
                              showEditMenu = false; showEncodingMenu = true
                          }
                          EditorMenuOption(stringResource(R.string.editor_word_wrap), Icons.AutoMirrored.Filled.WrapText,
                              enabled = entry != null && !state.loading,
                              checked = state.viewState.wordWrapMode == WordWrapMode.VIEWPORT) {
                              showEditMenu = false
                              state.viewState.wordWrapMode = if (state.viewState.wordWrapMode == WordWrapMode.OFF)
                                  WordWrapMode.VIEWPORT else WordWrapMode.OFF
                          }
                     }
                 }
             })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
             if (state.tabs.size > 1) {
                 EditorTabStrip(state.tabs, onSelectDocument, onCloseDocument)
             }
             if (state.recoveredDraft != null) {
                 DraftRecoveryBanner(
                     onRestore = onRestoreDraft,
                     onDiscard = onDiscardDraft
                 )
             }
            if (state.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                 Text(stringResource(R.string.editor_opening), Modifier.padding(16.dp))
            } else if (entry == null) {
                EditorEmptyStateView(onExplore = onExplore)
            } else {
                 if (state.formatting || state.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
                  if (showSearch) {
                      val searchShape = RoundedCornerShape(12.dp)
                      TextField(state.search, onValueChange = onSearch,
                          modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenStyle.pagePadding, vertical = 3.dp)
                              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, searchShape)
                              .focusRequester(searchFocus).onPreviewKeyEvent {
                                  if (it.type == KeyEventType.KeyDown && it.key == Key.Enter) { onFindNext(it.isShiftPressed); true }
                                  else if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) { showSearch = false; true } else false
                              }, singleLine = true, shape = searchShape,
                          placeholder = { Text(stringResource(R.string.editor_find_in_file), maxLines = 1) },
                          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                          trailingIcon = {
                              Row(verticalAlignment = Alignment.CenterVertically) {
                                  IconButton(onClick = { dispatch(EditorAction.FIND_PREVIOUS) },
                                      enabled = enabled(EditorAction.FIND_PREVIOUS), modifier = Modifier.size(40.dp)) {
                                      Icon(Icons.Filled.ArrowUpward, contentDescription = stringResource(R.string.editor_previous))
                                  }
                                  IconButton(onClick = { dispatch(EditorAction.FIND_NEXT) },
                                      enabled = enabled(EditorAction.FIND_NEXT), modifier = Modifier.size(40.dp)) {
                                      Icon(Icons.Filled.ArrowDownward, contentDescription = stringResource(R.string.editor_next))
                                  }
                                  IconButton(onClick = { showSearch = false }, modifier = Modifier.size(40.dp)) {
                                      Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.editor_close_search))
                                  }
                              }
                          }, colors = editorFieldColors())
                      if (showReplace) {
                          TextField(state.replacement, onReplacement, singleLine = true,
                              placeholder = { Text(stringResource(R.string.editor_replacement)) },
                              leadingIcon = { Icon(Icons.Filled.FindReplace, contentDescription = null) },
                              modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenStyle.pagePadding, vertical = 3.dp),
                              shape = searchShape, colors = editorFieldColors())
                      }
                      Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                          .padding(horizontal = ScreenStyle.pagePadding, vertical = 6.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                          EditorSearchChip(stringResource(R.string.editor_case_sensitive), state.searchOptions.caseSensitive) {
                              onSearchOptions(state.searchOptions.copy(caseSensitive = !state.searchOptions.caseSensitive))
                          }
                          EditorSearchChip(stringResource(R.string.editor_whole_word), state.searchOptions.wholeWord) {
                              onSearchOptions(state.searchOptions.copy(wholeWord = !state.searchOptions.wholeWord))
                          }
                          EditorSearchChip(stringResource(R.string.editor_regex), state.searchOptions.regex) {
                              onSearchOptions(state.searchOptions.copy(regex = !state.searchOptions.regex))
                          }
                          if (showReplace) {
                              Spacer(Modifier.width(4.dp))
                              OutlinedButton(onClick = { onReplace(false) }, enabled = enabled(EditorAction.FIND_NEXT),
                                  modifier = Modifier.height(32.dp), shape = RoundedCornerShape(16.dp),
                                  contentPadding = PaddingValues(horizontal = 12.dp)) {
                                  Text(stringResource(R.string.editor_replace_current), style = MaterialTheme.typography.labelSmall)
                              }
                              Button(onClick = { onReplace(true) },
                                  enabled = enabled(EditorAction.FIND_NEXT) && !state.searchResult.truncated,
                                  modifier = Modifier.height(32.dp), shape = RoundedCornerShape(16.dp),
                                  colors = ButtonDefaults.buttonColors(
                                      containerColor = StructaPurple,
                                      contentColor = Color.White
                                  ),
                                  contentPadding = PaddingValues(horizontal = 12.dp)) {
                                  Text(stringResource(R.string.editor_replace_all), style = MaterialTheme.typography.labelSmall)
                              }
                          } else {
                              Spacer(Modifier.width(6.dp))
                              Text(state.searchResult.error ?: if (state.searchResult.truncated)
                                  pluralStringResource(R.plurals.editor_matches_truncated, state.occurrences, state.occurrences)
                                  else pluralStringResource(R.plurals.editor_matches_count, state.occurrences, state.occurrences),
                                  style = MaterialTheme.typography.labelSmall, maxLines = 1)
                          }
                      }
                      if (showReplace && (state.searchResult.error != null || state.searchResult.truncated)) {
                          Text(state.searchResult.error ?: pluralStringResource(R.plurals.editor_matches_truncated,
                              state.occurrences, state.occurrences), Modifier.padding(horizontal = ScreenStyle.pagePadding),
                              style = MaterialTheme.typography.labelSmall)
                      }
                      if (state.searching || state.replacing) LinearProgressIndicator(Modifier.fillMaxWidth())
                  }
                  state.inputSession?.let { input ->
                      if (isMarkdown && !state.isEditable && renderMarkdown) {
                          val markdownText = remember(state.contentVersion, input.engine.document) {
                              val buf = input.engine.document.buffer
                              buf.getText(0, buf.length).toString()
                          }
                          val markdownNodes = remember(markdownText) {
                              MarkdownParser.parse(markdownText)
                          }
                          MarkdownViewer(
                              nodes = markdownNodes,
                              modifier = Modifier.weight(1f).fillMaxWidth()
                          )
                      } else {
                          StructaEditor(input.engine, Modifier.weight(1f).fillMaxWidth(),
                              contentVersion = state.contentVersion, cursorVisible = state.isEditable,
                              editable = state.isEditable, inputSession = input, viewState = state.viewState,
                              fileName = entry.name, language = state.highlightLanguage(), searchMatches = state.searchResult.matches,
                              selectedMatch = state.selectedMatch, font = editorFont, fontSize = editorFontSize,
                              onEditorAction = ::dispatch)
                      }

                      if (showAccessoryBar && state.isEditable) {
                          val accessorySymbols = remember {
                              listOf("{", "}", "(", ")", "[", "]", ";", "=", ":", "\"", "'", "/", "\\", "|", "&", "!", "<", ">", "$", "_", "Tab")
                          }
                          LazyRow(
                              modifier = Modifier
                                  .fillMaxWidth()
                                  .background(MaterialTheme.colorScheme.surfaceContainer)
                                  .padding(horizontal = 4.dp, vertical = 2.dp),
                              horizontalArrangement = Arrangement.spacedBy(4.dp)
                          ) {
                              items(accessorySymbols.size) { idx ->
                                  val sym = accessorySymbols[idx]
                                  Surface(
                                      onClick = {
                                          if (sym == "Tab") {
                                              input.execute(SmartInsertTextCommand("    "))
                                          } else {
                                              input.execute(SmartInsertTextCommand(sym))
                                          }
                                      },
                                      shape = RoundedCornerShape(6.dp),
                                      color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                      modifier = Modifier.height(30.dp)
                                  ) {
                                      Box(
                                          modifier = Modifier.padding(horizontal = 10.dp),
                                          contentAlignment = Alignment.Center
                                      ) {
                                          Text(
                                              text = sym,
                                              style = MaterialTheme.typography.bodyMedium,
                                              color = MaterialTheme.colorScheme.onSurface
                                          )
                                      }
                                  }
                              }
                          }
                      }
                  }
            }
        }
    }
    var outlineSymbols by remember { mutableStateOf<List<DocumentSymbol>>(emptyList()) }
    var outlineLoading by remember { mutableStateOf(false) }

    LaunchedEffect(showOutline) {
        if (showOutline) {
            outlineLoading = true
            val session = state.inputSession
            val langId = state.highlightLanguage().id
            val symbols = withContext(Dispatchers.Default) {
                val text = session?.buffer?.let { buf ->
                    val len = buf.length.coerceAtMost(DocumentSymbolExtractor.MAX_ANALYSIS_CHARS)
                    buf.getText(0, len)
                } ?: ""
                DocumentSymbolExtractor.extract(text, langId)
            }
            outlineSymbols = symbols
            outlineLoading = false
        }
    }

    if (showOutline) {
        DocumentOutlinePicker(
            symbols = outlineSymbols,
            loading = outlineLoading,
            onSelect = { sym ->
                state.inputSession?.execute(MoveCursorCommand(TextOffset(sym.offset)))
                onLine(sym.line + 1)
                showOutline = false
            },
            onDismiss = { showOutline = false }
        )
    }
    if (showGoToLine) {
        AlertDialog(
            onDismissRequest = { showGoToLine = false },
            modifier = Modifier.border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(24.dp)
            ),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.dialog_editor_go_to_line),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                LaunchedEffect(Unit) { withFrameNanos { }; lineFocus.requestFocus() }
                OutlinedTextField(
                    value = requestedLine,
                    onValueChange = { requestedLine = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth().focusRequester(lineFocus).onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyDown && it.key == Key.Enter && requestedLine.toIntOrNull()?.let { n -> n > 0 } == true) {
                            onLine(requestedLine.toInt()); showGoToLine = false; true
                        } else if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) { showGoToLine = false; true } else false
                    },
                    singleLine = true,
                    label = { Text(stringResource(R.string.dialog_editor_line_number)) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedBorderColor = StructaPurple,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            },
            confirmButton = {
                Button(
                    enabled = requestedLine.toIntOrNull()?.let { it > 0 } == true,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StructaPurple, contentColor = Color.White),
                    onClick = { requestedLine.toIntOrNull()?.let(onLine); showGoToLine = false }
                ) {
                    Text(stringResource(R.string.editor_accept), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showGoToLine = false },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
    if (showSaveAsDialog && entry != null) {
        val currentPath = entry.id.value
        val parentFolder = remember(currentPath) {
            val f = java.io.File(currentPath)
            f.parent ?: android.os.Environment.getExternalStorageDirectory().absolutePath
        }
        SaveAsDialog(
            currentName = entry.name,
            initialParentPath = parentFolder,
            onSave = { folder, newName ->
                val dir = java.io.File(folder)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val destinationFile = java.io.File(dir, newName)
                onSaveAs(DocumentId(destinationFile.canonicalPath))
                showSaveAsDialog = false
            },
            onDismiss = {
                showSaveAsDialog = false
            }
        )
    }
    if (showFormatMenu) {
        LanguageSelectionDialog(
            currentLanguage = state.highlightLanguage(),
            onSelect = { language ->
                state.inputSession?.finishComposingText()
                onLanguage(language)
                showFormatMenu = false
            },
            onDismiss = { showFormatMenu = false }
        )
    }
    if (showEncodingMenu) {
        EncodingSelectionDialog(
            currentCharset = state.charset,
            onSelect = { charset ->
                state.inputSession?.finishComposingText()
                onCharset(charset)
                showEncodingMenu = false
            },
            onAutoDetect = {
                state.inputSession?.finishComposingText()
                onAutoDetectCharset()
                showEncodingMenu = false
            },
            onDismiss = { showEncodingMenu = false }
        )
    }
    state.pendingClose?.let { id ->
        val title = state.tabs.firstOrNull { it.documentId == id }?.title.orEmpty()
        AlertDialog(
            onDismissRequest = onCancelClose,
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
                        imageVector = Icons.Filled.WarningAmber,
                        contentDescription = null,
                        tint = StatusOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.editor_close_named, title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_editor_unsaved_changes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onSaveClose,
                    enabled = !state.saving && state.pendingSave == null,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StructaPurple,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.dialog_editor_save_close),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDiscardClose,
                        enabled = !state.saving,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed)
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_editor_discard),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedButton(
                        onClick = onCancelClose,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            }
        )
    }
}

@Composable
private fun EditorTabStrip(tabs: List<EditorTab>, onSelect: (DocumentId) -> Unit,
    onClose: (DocumentId) -> Unit) {
    val listState = rememberLazyListState()
    val activeIndex = tabs.indexOfFirst { it.active }
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) listState.animateScrollToItem(activeIndex)
    }
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(Modifier.fillMaxWidth().background(colors.surface)) {
        val tabWidth = if (tabs.size == 2) maxWidth / 2 else 196.dp
        Column {
            LazyRow(Modifier.fillMaxWidth(), state = listState) {
                itemsIndexed(tabs, key = { _, tab -> tab.documentId.value }) { _, tab ->
                    val titleColor = if (tab.active) colors.onSurface else colors.onSurfaceVariant
                    val shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    val tabDescription = if (tab.dirty) stringResource(R.string.editor_dirty_title, tab.title) else tab.title
                    Column(Modifier.width(tabWidth)) {
                        Row(Modifier.fillMaxWidth().height(52.dp).clip(shape)
                            .background(if (tab.active) colors.surfaceContainerHigh else colors.surface)
                            .selectable(selected = tab.active, role = Role.Tab,
                                onClick = { onSelect(tab.documentId) })
                            .semantics { contentDescription = tabDescription }
                            .padding(start = 14.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null,
                                modifier = Modifier.size(18.dp), tint = titleColor)
                            Spacer(Modifier.width(10.dp))
                            Text(tab.title, modifier = Modifier.weight(1f), maxLines = 1,
                                overflow = TextOverflow.Ellipsis, color = titleColor,
                                style = MaterialTheme.typography.titleSmall)
                            if (tab.dirty) {
                                Spacer(Modifier.width(8.dp))
                                Box(Modifier.size(8.dp).clip(RoundedCornerShape(50))
                                    .background(colors.primary))
                            }
                            IconButton(onClick = { onClose(tab.documentId) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.editor_close_named, tab.title),
                                    modifier = Modifier.size(18.dp), tint = colors.onSurfaceVariant)
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(3.dp)
                            .background(if (tab.active) colors.primary else Color.Transparent))
                    }
                }
            }
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = .45f))
        }
    }
}

@Composable
private fun EditorMenuOption(label: String, icon: ImageVector, shortcut: String? = null,
    enabled: Boolean = true, submenu: Boolean = false, checked: Boolean = false, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(label, maxLines = 1) }, onClick = onClick, enabled = enabled,
        leadingIcon = { Icon(icon, contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f)) },
        trailingIcon = when {
            checked -> {{ Icon(Icons.Filled.Check, contentDescription = null) }}
            shortcut != null -> {{ Text(shortcut, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant) }}
            submenu -> {{ Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) }}
            else -> null
        })
}

@Composable
private fun editorFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)

@Composable
private fun EditorSearchChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(shape)
            .background(if (selected) StructaPurple.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(
                1.dp,
                if (selected) StructaPurple.copy(alpha = 0.60f) else MaterialTheme.colorScheme.outlineVariant,
                shape
            )
            .selectable(selected = selected, role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) StructaLavender else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EditorEmptyStateView(onExplore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            val iconShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(iconShape)
                    .background(StructaPurple.copy(alpha = 0.15f))
                    .border(1.dp, StructaPurple.copy(alpha = 0.30f), iconShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = StructaLavender
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.editor_no_file),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.editor_no_file_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onExplore,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StructaPurple,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.editor_go_explore),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: Language,
    onSelect: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    StructaPurple.copy(alpha = 0.15f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Code,
                                contentDescription = null,
                                tint = StructaPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.editor_language_dialog),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.editor_language_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.common_cancel),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // List of languages
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageRegistry.languages.forEach { language ->
                        val isSelected = currentLanguage.id == language.id
                        LanguageOptionCard(
                            language = language,
                            isSelected = isSelected,
                            onClick = { onSelect(language) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionCard(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (badgeText, badgeColor, description) = when (language.id) {
        "markdown" -> Triple("MD", StructaLavender, "Documentación, notas y vista previa")
        "json" -> Triple("{ }", StatusOrange, "Estructuras de datos · Formateo")
        "yaml" -> Triple("YML", StructaCyan, "Configuración legible · Formateo")
        "xml" -> Triple("< >", Color(0xFFFB923C), "Marcado y layouts · Formateo")
        "go" -> Triple("GO", Color(0xFF00ADD8), "Código fuente Go · Sintaxis")
        "text" -> Triple("TXT", DarkTextSecondary, "Texto plano sin resaltado")
        else -> Triple(language.id.take(3).uppercase(), MaterialTheme.colorScheme.primary, "")
    }

    val backgroundColor = if (isSelected) {
        StructaPurple.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f)
    }

    val borderColor = if (isSelected) {
        StructaPurple.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = languageName(language),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(StructaPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveAsDialog(
    currentName: String,
    initialParentPath: String,
    onSave: (folder: String, fileName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var destinationFolder by rememberSaveable(initialParentPath) { mutableStateOf(initialParentPath) }
    var fileName by rememberSaveable(currentName) { mutableStateOf(currentName) }
    var showFolderPicker by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showFolderPicker) {
        FolderPickerDialog(
            initialPath = destinationFolder,
            onSelectFolder = { chosen ->
                destinationFolder = chosen
                showFolderPicker = false
            },
            onDismiss = { showFolderPicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    .background(StructaPurple.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SaveAs,
                    contentDescription = null,
                    tint = StructaPurple,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.editor_save_as),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Section 1: Folder location
                Text(
                    text = "Carpeta de destino",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFolderPicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = null,
                            tint = StructaLavender,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = destinationFolder,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = { showFolderPicker = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = StructaPurple.copy(alpha = 0.2f),
                                contentColor = StructaLavender
                            ),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Cambiar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Section 2: File name
                Text(
                    text = "Nombre del archivo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = fileName,
                    onValueChange = {
                        fileName = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            "ej. documento.txt",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = StatusRed) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedBorderColor = StructaPurple,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = fileName.trim()
                    val trimmedFolder = destinationFolder.trim()
                    if (trimmedFolder.isBlank()) {
                        errorMessage = "La carpeta de destino no puede estar vacía"
                    } else if (trimmedName.isBlank() || trimmedName.contains('/') || trimmedName.contains('\\')) {
                        errorMessage = "Nombre de archivo no válido"
                    } else {
                        onSave(trimmedFolder, trimmedName)
                    }
                },
                enabled = fileName.isNotBlank() && destinationFolder.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StructaPurple,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.editor_save), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}


@Composable
fun DraftRecoveryBanner(
    onRestore: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF141418),
        border = BorderStroke(1.dp, StatusOrange.copy(alpha = 0.5f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = StatusOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.draft_recovery_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.draft_recovery_desc),
                style = MaterialTheme.typography.bodySmall,
                color = DarkTextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDiscard,
                    colors = ButtonDefaults.textButtonColors(contentColor = DarkTextSecondary)
                ) {
                    Text(stringResource(R.string.draft_recovery_discard), style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onRestore,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StructaPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        stringResource(R.string.draft_recovery_restore),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

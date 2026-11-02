package com.edro08.structa.data.settings

import android.content.Context
import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.filesystem.FileRef
import com.edro08.structa.domain.settings.SettingsRepository
import com.edro08.structa.domain.settings.EditorFont

class AndroidSettingsRepository(context: Context, name: String = "structa") : SettingsRepository {
    private val preferences = context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)

    override fun lastFolder(): DocumentId? =
        preferences.getString("last_folder_uri", null)?.let(::DocumentId)

    override fun setLastFolder(folder: DocumentId) {
        setLastFolderRef(FileRef.of(folder))
    }

    override fun lastFolderRef(): FileRef? {
        val folder = lastFolder() ?: return null
        val path = folder.value
        val isAbsolute = path.startsWith("/") ||
            (path.length >= 3 && path[0].isLetter() && path[1] == ':' && (path[2] == '\\' || path[2] == '/'))
        if (!isAbsolute) {
            preferences.edit().remove("last_folder_uri").remove("last_folder_provider").apply()
            return null
        }
        return FileRef(folder, "direct")
    }

    override fun setLastFolderRef(folder: FileRef) {
        preferences.edit().putString("last_folder_uri", folder.id.value)
            .putString("last_folder_provider", folder.fileSystem).apply()
    }

    override fun darkTheme(): Boolean = preferences.getBoolean("dark_theme", true)
    override fun setDarkTheme(dark: Boolean) { preferences.edit().putBoolean("dark_theme", dark).apply() }

    override fun editorFont(): EditorFont = EditorFont.entries.firstOrNull {
        it.name == preferences.getString("editor_font", null)
    } ?: EditorFont.MONOSPACE
    override fun setEditorFont(font: EditorFont) { preferences.edit().putString("editor_font", font.name).apply() }

    override fun editorFontSize(): Int = preferences.getInt("editor_font_size", 14).coerceIn(10, 32)
    override fun setEditorFontSize(size: Int) {
        require(size in 10..32)
        preferences.edit().putInt("editor_font_size", size).apply()
    }

    override fun showAccessoryBar(): Boolean = preferences.getBoolean("show_accessory_bar", true)
    override fun setShowAccessoryBar(show: Boolean) {
        preferences.edit().putBoolean("show_accessory_bar", show).apply()
    }
}

package com.edro08.structa.ui.editor.input

import android.view.KeyEvent
import com.edro08.structa.domain.editor.command.*
import com.edro08.structa.domain.editor.cursor.TextOffset

/** Shared application command registry: palette, toolbar and keyboard dispatch these same intents. */
enum class EditorAction(val title: String, val shortcut: String = "") {
    SAVE("Guardar", "Ctrl+S"), FIND("Buscar", "Ctrl+F"), REPLACE("Reemplazar", "Ctrl+H"),
    GO_TO_LINE("Ir a línea", "Ctrl+G"), QUICK_OPEN("Abrir", "Ctrl+P"),
    COMMAND_PALETTE("Command Palette", "Ctrl+Shift+P"), FIND_NEXT("Buscar siguiente", "F3"),
    FIND_PREVIOUS("Buscar anterior", "Shift+F3"), UNDO("Deshacer", "Ctrl+Z"),
    REDO("Rehacer", "Ctrl+Shift+Z"), FORMAT("Formatear"), CLOSE("Cerrar archivo"),
    DUPLICATE_LINE("Duplicar línea", "Ctrl+D"),
    DELETE_LINE("Eliminar línea", "Ctrl+Shift+K"),
    DOCUMENT_OUTLINE("Índice de contenido", "Ctrl+Shift+O"),
    GLOBAL_SEARCH("Buscar en Workspace", "Ctrl+Shift+F"),
    TOGGLE_EDITABLE("Alternar edición", "Ctrl+E")
}

fun applicationActionFor(event: KeyEvent): EditorAction? {
    if (event.action != KeyEvent.ACTION_DOWN || event.isAltPressed) return null
    if (event.keyCode == KeyEvent.KEYCODE_F3) return if (event.isShiftPressed) EditorAction.FIND_PREVIOUS else EditorAction.FIND_NEXT
    if (!event.isCtrlPressed && !event.isMetaPressed) return null
    return when (event.keyCode) {
        KeyEvent.KEYCODE_S -> EditorAction.SAVE
        KeyEvent.KEYCODE_E -> EditorAction.TOGGLE_EDITABLE
        KeyEvent.KEYCODE_F -> if (event.isShiftPressed) EditorAction.GLOBAL_SEARCH else EditorAction.FIND
        KeyEvent.KEYCODE_H -> EditorAction.REPLACE
        KeyEvent.KEYCODE_G -> EditorAction.GO_TO_LINE
        KeyEvent.KEYCODE_O -> if (event.isShiftPressed) EditorAction.DOCUMENT_OUTLINE else null
        KeyEvent.KEYCODE_P -> if (event.isShiftPressed) EditorAction.COMMAND_PALETTE else EditorAction.QUICK_OPEN
        else -> null
    }
}

class KeyBindingHandler(
    private val input: EditorInputSession,
    private val clipboardAction: (Int) -> Boolean,
    private val applicationAction: (EditorAction) -> Unit
) {
    fun handle(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false
        applicationActionFor(event)?.let { action(it); return true }
        if ((event.isCtrlPressed || event.isMetaPressed) && !event.isAltPressed) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_Z -> input.execute(if (event.isShiftPressed) RedoCommand else UndoCommand)
                KeyEvent.KEYCODE_Y -> input.execute(RedoCommand)
                KeyEvent.KEYCODE_A -> input.selectAll()
                KeyEvent.KEYCODE_C -> clipboardAction(android.R.id.copy)
                KeyEvent.KEYCODE_X -> clipboardAction(android.R.id.cut)
                KeyEvent.KEYCODE_V -> clipboardAction(android.R.id.paste)
                KeyEvent.KEYCODE_K -> if (event.isShiftPressed) input.execute(DeleteLineCommand) else return false
                KeyEvent.KEYCODE_D -> input.execute(DuplicateLineCommand(down = true))
                KeyEvent.KEYCODE_MOVE_HOME -> input.execute(MoveCursorCommand(TextOffset(0), event.isShiftPressed), false)
                KeyEvent.KEYCODE_MOVE_END -> input.execute(MoveCursorCommand(TextOffset(input.buffer.length), event.isShiftPressed), false)
                else -> return false
            }
            return true
        }
        if (event.isAltPressed && !event.isCtrlPressed && !event.isMetaPressed) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (event.isShiftPressed) input.execute(DuplicateLineCommand(down = false))
                    else input.execute(MoveLineCommand(down = false))
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (event.isShiftPressed) input.execute(DuplicateLineCommand(down = true))
                    else input.execute(MoveLineCommand(down = true))
                    return true
                }
            }
        }
        when (event.keyCode) {
            KeyEvent.KEYCODE_DEL -> input.execute(SmartDeleteBackwardCommand)
            KeyEvent.KEYCODE_FORWARD_DEL -> input.execute(DeleteForwardCommand)
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> input.execute(SmartInsertTextCommand("\n"))
            KeyEvent.KEYCODE_TAB -> input.execute(IndentLinesCommand(unindent = event.isShiftPressed))
            KeyEvent.KEYCODE_DPAD_LEFT -> input.execute(if (event.isShiftPressed) SelectLeftCommand else MoveCursorLeftCommand, false)
            KeyEvent.KEYCODE_DPAD_RIGHT -> input.execute(if (event.isShiftPressed) SelectRightCommand else MoveCursorRightCommand, false)
            KeyEvent.KEYCODE_MOVE_HOME -> input.execute(if (event.isShiftPressed) SelectHomeCommand else MoveCursorHomeCommand, false)
            KeyEvent.KEYCODE_MOVE_END -> input.execute(if (event.isShiftPressed) SelectEndCommand else MoveCursorEndCommand, false)
            KeyEvent.KEYCODE_DPAD_UP -> moveVertical(-1, event.isShiftPressed)
            KeyEvent.KEYCODE_DPAD_DOWN -> moveVertical(1, event.isShiftPressed)
            else -> {
                val codePoint = event.unicodeChar
                if (codePoint <= 0 || !Character.isValidCodePoint(codePoint)) return false
                input.execute(SmartInsertTextCommand(String(Character.toChars(codePoint))))
            }
        }
        return true
    }

    private fun action(action: EditorAction) {
        input.finishComposingText()
        applicationAction(action)
    }

    private fun moveVertical(delta: Int, extend: Boolean) {
        val buffer = input.buffer
        val currentLine = buffer.getLineForOffset(input.active)
        val column = input.active - buffer.getLineStart(currentLine)
        val line = (currentLine + delta).coerceIn(0, buffer.lineCount - 1)
        val start = buffer.getLineStart(line)
        val offset = start + minOf(column, buffer.getLineEnd(line) - start)
        input.execute(MoveCursorCommand(TextOffset(offset), extend), false)
    }
}

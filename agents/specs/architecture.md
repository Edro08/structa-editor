# Structa Architecture Specification

Structa is a modular Android code editor built with Kotlin and Jetpack Compose. Its architectural cornerstone is the **strict separation between the editor engine (pure Kotlin domain, zero Android dependencies) and the presentation/storage layers**.

Codebase root: `app/src/main/java/com/edro08/structa/`

---

## 🏛️ Layer Organization & Dependency Boundaries

```
           [ UI / Presentation ]
           (ui/editor/, ui/screen/)
                 /          \
                /            \
   (uses domain interfaces)   \ (invokes use cases)
              /                \
             v                  v
   [ Domain (Pure Kotlin) ] <--- [ Application Use Cases ]
   domain/editor/                application/
   domain/filesystem/
   domain/workspace/
             ^
             | (implements domain interfaces: FileSystem, SessionRepository...)
             |
   [ Data / Infrastructure ]
   data/filesystem/
   data/settings/

   * Composition Root: app/ (AppContainer) wires Data implementations
     into Domain interfaces and injects them into ViewModels and Use Cases.
```

| Layer | Package | Core Responsibilities & Invariants |
| --- | --- | --- |
| **Editor Core (Domain)** | `domain/editor/` | **Pure Kotlin only.** Text data structures, coordinate calculations, transactional history, decoupled commands, search, and syntax tokenization. **Strict invariant: Must NEVER import `android.*` or `androidx.*`.** |
| **Filesystem (Domain)** | `domain/filesystem/`, `domain/workspace/` | Filesystem abstractions (`FileSystem`, `FileRef`, `FileMetadata`), workspace metadata, and session contracts. Completely decoupled from concrete storage providers. |
| **Application** | `application/` | Orchestration use cases: reading/writing documents, formatters, and workspace coordination. Bridges UI requests to domain engines and storage providers. |
| **Data / Storage** | `data/filesystem/` | `RoutedFileSystem` dispatching by provider to `SafFileSystem` (Storage Access Framework) or `DirectFileSystem` (POSIX direct access). Handles UTF-8 encoding. |
| **Settings / State** | `data/settings/` | User preferences, workspace history, and session state persistence via `SharedPreferences`. |
| **Presentation / Renderer** | `ui/editor/`, `ui/screen/` | Low-level `Canvas` rendering (`StructaEditorView`), IME input bridge (`EditorInputConnection`), Jetpack Compose screens, and ViewModels. |
| **DI / App Lifecycle** | `app/` | Manual dependency injection via `AppContainer`, system intent routing, and navigation scaffold in `StructaApp`. |

---

## ⚙️ Editor Core Subsystems (`domain/editor/`)

The editor core operates on an immutable, reactive command pattern. It is partitioned into six cohesive subsystems:

### 1. Text Storage & Coordinate System
- **Piece Table (`PieceTableBuffer`):** The document is stored as a Piece Table rather than a contiguous string or mutable line array.
  - *Original Buffer:* Immutable string loaded on file open.
  - *Add Buffer:* Append-only string storing all newly inserted characters.
  - *Piece Sequence:* Lightweight descriptors `(bufferType, offset, length)` reconstructing the logical document. Inserts and deletions run without bulk memory copies.
- **Incremental Line Index (`LineIndex`):** Tracks newline offsets (`LF` and `CRLF`) incrementally upon each mutation. Resolves line by index in $O(1)$ and offset-to-line in $O(\log n)$ via binary search.
- **UTF-16 Coordinate Space:** Offsets, semi-open ranges `[start, end)`, and selections are indexed in UTF-16 code units to match Java/Android runtime string representations. Zero-based line indexing.

### 2. Editing Session & Transactional History
- **`EditingSession`:** Encapsulates transient editing state: cursor position, directional selection (anchor and active end), insertion, replacement, and forward/backward deletion.
- **Transactional History (`EditHistory`):**
  - Avoids full-document snapshotting to minimize GC pressure.
  - Stores atomic, reversible operations (`Insert`, `Delete`, `Replace`).
  - **Explicit Transactions:** Groups multiple atomic edits (e.g., typing a word, batch IME commit, or multi-line replace) into a single Undo/Redo step. Correctly restores text, cursor position, and selection state.

### 3. Document, Engine, and Commands
- **`EditorDocument`:** State container independent of UI and filesystem. Bundles buffer, history, and revisions (`revision` vs `savedRevision`) to track the dirty state.
- **`EditorEngine`:** The sole coordinator permitted to mutate document state. Executes decoupled `EditorCommand` instances (`InsertTextCommand`, `MoveCursorCommand`, `UndoCommand`, etc.).
- **Per-Tab Isolation:** Each open tab owns an isolated `EditorEngine`, `EditorDocument`, and input session to prevent race conditions or cross-tab state leakage.

### 4. Custom Canvas Renderer & Virtualized Viewport (`StructaEditorView`)
- **Direct Canvas Drawing:** Instead of using standard Android `EditText` or `TextView`, the editor uses a custom Android `View` drawing directly onto `android.graphics.Canvas` with strict monospace font metrics.
- **Virtualized Viewport:** Measures and renders only visible lines plus a 2-line overscan margin on each side. Maintains steady 60/120 fps scrolling even on 100,000+ line documents.
- **Long-Line Optimization:** Uses checkpoints every 256 UTF-16 units to calculate horizontal layout offsets without laying out full lines spanning thousands of characters.
- **Gutter & Decorators:** Renders sticky line-number gutter, active line background, blinking cursor, and overlay decorations (search highlights and syntax spans).

### 5. Virtual Keyboard Integration (IME Bridge)
- **`EditorInputConnection`:** Custom implementation of Android's `InputConnection` bridging the `View` directly to virtual keyboards (Gboard, SwiftKey, Samsung Keyboard) without an intermediate `Editable`.
- **Text Composition:** Correctly handles IME predictive text and composition batches, converting them into atomic engine transactions.
- **Dynamic Viewport Resize:** Adjusts viewport bounds and keeps the active cursor in view when the software keyboard opens or resizes.
- **Hardware Keyboard Support:** Modifier shortcuts (`Ctrl`, `Meta`, `Alt`, `Shift`) for navigation, selection, and document actions.

### 6. Background Processing & Immutability
- Search (`DocumentSearch`) and syntax highlighting (`IncrementalHighlighter`) **never block the main UI thread**.
- Both consume **immutable snapshots** of the document produced by the engine.
- If edits occur while a background task is running, outdated jobs are cooperatively cancelled and their results safely discarded.

---

## 📁 Filesystem Architecture

Storage access is abstracted behind `FileSystem` and dispatched via `RoutedFileSystem`:

1. **Storage Access Framework (`SafFileSystem`):**
   - Interacts with Android's `DocumentsContract` via `content://` URIs.
   - Allows users to select files/folders using the system picker without elevated permissions.
2. **Direct Shared Storage (`DirectFileSystem`):**
   - Direct POSIX file path access (`/storage/emulated/0/...`).
   - Requires `MANAGE_EXTERNAL_STORAGE` permission.
   - Essential for fast workspace indexing, recursive directory traversal, and programming projects.
   - Enforces a security boundary blocking protected system paths (`Android/data` and `Android/obb`).

---

## 📱 Presentation & Compose UI Flow

Built with **Jetpack Compose**:

- **Home Screen (`HomeScreen`):** Recent and favorite workspaces, history management, and new workspace picker.
- **Explorer Screen (`ExplorerScreen`):** Workspace file tree with lazy node loading, breadcrumb navigation, quick search filter, and file CRUD operations (create, rename, delete). Enforces closing open editor tabs before renaming or deleting active files.
- **Direct Picker:** Specialized explorer mode (`SELECT_DIRECTORY`) for choosing root folders under direct storage.
- **Editor Screen (`EditorScreen`):** Primary workspace.
  - Central `StructaEditorView` hosting the virtualized canvas.
  - Tab bar with unsaved change indicators (*dirty dots*).
  - **File Menu:** Quick Open (`Ctrl+P`), Save, Save As, Save All, and Tab management.
  - **Edit Menu:** Find & Replace (Regex/literal), Go to Line, Undo/Redo, Format (JSON/YAML/XML), Word Wrap toggle, and Language Selector.
  - Bottom navigation bar is hidden on the editor screen to maximize vertical real estate.
- **Settings Screen (`SettingsScreen`):** Theme toggle (Light/Dark), monospace typography selection, font size slider (10–32 sp), and file provider permission management.

# Fases de desarrollo de Structa

Las fases **1–16** representan el desarrollo del motor base, arquitectura, ergonomía técnica, refinamiento móvil y visualización estructurada (completadas). Las fases subsiguientes definen la hoja de ruta técnica de Structa.

---

## 🟢 Fases completadas

### Fase 1 — Buffer y posiciones
- `PieceTableBuffer` con almacén append-only y piezas que representan el texto lógico sin recrear buffers completos.
- `LineIndex` incremental. Conversión de coordenadas entre offsets UTF-16 y línea/columna en $O(\log n)$.
- Pruebas exhaustivas de transacciones, límites y consistencia UTF-16.

### Fase 2 — Operaciones de edición
- `EditingSession`: inserción, reemplazo de selección, borrado hacia atrás/delante, navegación y anclaje de selección.
- Gestión de límites de cursor y colapso de selección mediante flechas del teclado.

### Fase 3 — Historial de transacciones
- `EditHistory` con operaciones inversibles (`Insert`, `Delete`, `Replace`) agrupadas en transacciones explícitas.
- Soporte para Undo y Redo restaurando posición del cursor y dirección de la selección sin almacenar snapshots de texto.

### Fase 4 — Documento y Engine
- `EditorDocument` desacoplado de la UI de Android y del sistema de archivos.
- `EditorEngine` como único punto de mutación mediante `EditorCommand`.

### Fase 5 — Estabilización de pruebas
- Aislamiento de la suite JVM en `domain.editor` sin dependencias de Android ni AndroidX.

### Fase 6 — Renderer personalizado (Canvas)
- `StructaEditorView` dibujando en `Canvas` directamente: viewport virtualizado (solo dibuja líneas visibles con margen), línea activa, selección, texto monoespaciado, cursor parpadeante y gutter con números de línea.
- Desplazamiento fluido en ambos ejes (scroll, fling, arrastre táctil y rueda de ratón).

### Fase 7 — Entrada táctil, teclado e IME
- `EditorInputSession` y `EditorInputConnection` adaptados al teclado virtual de Android (IME) sin usar `EditText`.
- Gestión de texto provisional (composición), atajos de teclado físico (`Ctrl`/`Meta`), menú contextual de selección y portapapeles.

### Fase 8 — Gestión de documentos y guardado
- Pestañas independientes con engine, documento y sesión propios.
- Control de estado sucio (*dirty indicator*) mediante revisiones lógicas. Guardado asíncrono y diálogo de confirmación de cambios al cerrar.

### Fase 9 — Filesystem directo y Explorador
- Acceso nativo POSIX mediante `DirectFileSystem` (`MANAGE_EXTERNAL_STORAGE`), eliminando la sobrecarga de SAF.
- Árbol de explorador de archivos con carga diferida y operaciones de crear, renombrar y borrar.
- Persistencia de sesión (pestañas abiertas, cursor, posición de scroll).

### Fase 10 — Productividad básica
- Búsqueda y reemplazo con expresiones regulares o texto literal, mayúsculas/minúsculas y palabra completa.
- Ir a línea por número (`Ctrl+G`).
- `Quick Open` (`Ctrl+P`) con búsqueda fuzzy sobre rutas relativas del workspace.

### Fase 11 — Resaltado léxico inicial
- Tokenización léxica incremental por líneas para **Go, JSON, YAML, Markdown y XML**.
- Soporte de decoraciones en el renderer (fondos de búsqueda, subrayados).
- Formateo de documentos para JSON, XML y YAML.

### Fase 12 — Ergonomía de edición
- Cierre automático de delimitadores `()`, `[]`, `{}`, `""`, `''` y backticks con salto de cierre y borrado en espejo.
- Indentación inteligente tras apertura de llaves y dos puntos; desindentación automática al cerrar.
- Manipulación de líneas: duplicar, mover arriba/abajo y eliminar línea completa.

### Fase 13 — Archivos flexibles y código Go
- Soporte de sintaxis estructurada para Markdown (`.md`), JSON (`.json`), YAML (`.yaml`), XML (`.xml`) y texto plano (`.txt`).
- Resaltado completo de palabras clave, operadores, funciones y strings de Go (`.go`).

### Fase 14 — Navegación de contenido y búsqueda global
- Búsqueda global en el workspace (`Ctrl+Shift+F`) en segundo plano con filtros de exclusión automáticos (`.git/`, `build/`, etc.).
- Document Outline (`Ctrl+Shift+O`): esquema interactivo de encabezados Markdown y funciones/tipos de Go con salto instantáneo.

### Fase 15 — Modo lectura y diseño Pure OLED
- Apertura en modo de visualización limpio por defecto sin levantar el teclado virtual.
- Selección táctil fluida con menú flotante de copiado (`Copiar` y `Seleccionar todo`) y alternancia a edición con `Ctrl+E` o botón de lápiz.
- Tema Pure OLED con negro absoluto (`#000000`), listas planas sin tarjetas en Inicio y Explorador, y botones en degradado Violeta a Índigo (`#7C3AED` a `#4F46E5`).

### Fase 16 — Vista limpia renderizada de Markdown
- Parser de Markdown en Kotlin puro (`MarkdownParser`) sin librerías externas ni WebViews: encabezados (H1–H6), listas, citas, código cercado y formato inline (negrita, cursiva, enlaces, código inline).
- Visor `MarkdownViewer` en Jetpack Compose sobre Pure OLED con bloques de código copiables y selección libre de texto.
- Apertura en vista limpia por defecto para `.md` en modo lectura con alternancia rápida a código fuente o edición (`Ctrl+E`).

---

## 🟡 Hoja de ruta técnica

### Fase 18 — Robustez móvil y persistencia (Draft Crash Recovery)
*Objetivo: Protección contra pérdida de datos en el ciclo de vida de Android.*
- Recuperación automática de borradores (*Draft Crash Recovery*) ante cierres inesperados, rotación o falta de memoria.

---

## 📊 Métricas de verificación

| Hito | Tests JVM aprobados | Tests Instrumentados aprobados |
| --- | ---: | ---: |
| Base inicial (Fases 1–11) | 137 | 28 |
| Ergonomía y búsqueda (Fases 12–14) | 189 | 54 |
| Modo lectura y Pure OLED (Fase 15) | 189 | 54 |
| Vista limpia Markdown (Fase 16) | 193 | 54 |

Comandos de verificación en Windows PowerShell:
```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin
```

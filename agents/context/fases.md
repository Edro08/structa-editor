# Fases de desarrollo de Structa

Las fases **1–11** representan el desarrollo del motor base y la arquitectura del editor (completadas). Las fases subsiguientes definen la hoja de ruta técnica de Structa como un **editor de texto técnico y archivos flexibles con visualización de código en Go**.

---

## 🟢 Fases completadas (Motor base y arquitectura)

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

### Fase 9 — Filesystem, Explorador y Sesión
- Capa de abstracción `FileSystem`: Storage Access Framework (SAF) y acceso directo (`DirectFileSystem` con `MANAGE_EXTERNAL_STORAGE`).
- Árbol de explorador de archivos con carga diferida y operaciones de crear, renombrar y borrar.
- Persistencia de sesión (pestañas abiertas, cursor, posición de scroll).

### Fase 10 — Productividad básica
- Búsqueda y reemplazo con expresiones regulares o texto literal, mayúsculas/minúsculas y palabra completa.
- Ir a línea por número.
- `Quick Open` (`Ctrl+P`) con búsqueda fuzzy sobre rutas relativas del workspace.

### Fase 11 — Resaltado léxico inicial
- Tokenización léxica incremental por líneas para **Go, JSON, YAML, Markdown y XML**.
- Soporte de decoraciones en el renderer (fondos de búsqueda, subrayados).
- Formateo de documentos para JSON, XML y YAML.

---

## 🟡 Evolución del núcleo (Estado actual de la base)

- **Archivos grandes:** Apertura de hasta 25 MB con edición fluida. Resaltado por ventanas visibles con checkpoints cada 128 líneas para documentos mayores a 1 MB.
- **Acceso dual:** Soporte para carpetas SAF y rutas absolutas directas (`/storage/emulated/0/...`), bloqueando accesos protegidos del sistema (`Android/data`).
- **Personalización:** Modos claro/oscuro, tipografías monoespaciadas y ajuste de tamaño de fuente (10–32 sp).

---

## 🚀 Hoja de ruta: Editor de texto técnico y archivos flexibles

Structa evoluciona hacia un **editor de texto técnico, fluido y enfocado en archivos flexibles** (Markdown, JSON, YAML, XML, Plain Text) con capacidad nativa para visualizar código en **Go**.

### Fase 12 — Ergonomía de edición (Smart Editing Core)
*Objetivo: Hacer que la escritura técnica sea ágil, natural y fluida en móvil.*
1. **Pares de delimitadores automáticos (Auto-closing pairs):**
   - Cierre automático de `()`, `[]`, `{}`, `""`, `''` y triple comilla/backticks.
   - Si se escribe el carácter de cierre estando frente a él, saltar hacia adelante en lugar de duplicarlo.
   - Envolver selección con el delimitador si hay texto seleccionado.
   - Borrado en espejo: al presionar Backspace entre delimitadores vacíos `(|)`, borrar ambos caracteres.
2. **Indentación inteligente (Smart Auto-indent):**
   - Al presionar Enter tras abrir una llave `{` o dos puntos `:`, aumentar un nivel de indentación en la línea siguiente y posicionar el cierre con la sangría correcta.
   - Desindentación automática al escribir la llave de cierre `}`.
3. **Manipulación de líneas (Atajos rápidos):**
   - Comentar / descomentar línea o selección (`Ctrl+/`).
   - Duplicar línea o selección (`Shift+Alt+Down` / `Shift+Alt+Up` o `Ctrl+D`).
   - Mover línea arriba / abajo (`Alt+Up` / `Alt+Down`).
   - Eliminar línea completa (`Ctrl+Shift+K`).

### Fase 13 — Archivos flexibles y visualización de código Go
*Objetivo: Soporte limpio para formatos estructurados y visualización de código en Go.*
1. **Formatos flexibles de texto y datos:**
   - **Markdown (`.md`)**: títulos, citas, listas, bloques de código, tablas y enlaces.
   - **JSON (`.json`, `.jsonl`)**: formateo, validación visual y claves/valores destacados.
   - **YAML (`.yaml`, `.yml`)**: formateo y jerarquía de datos.
   - **XML (`.xml`)**: formateo y etiquetas.
   - **Plain Text (`.txt`)**: texto sin formato de máximo rendimiento.
2. **Visualización de código en Go (`.go`):**
   - Resaltado léxico de palabras clave, operadores, funciones y strings crudos de Go.

### Fase 14 — Navegación de contenido y búsqueda global
*Objetivo: Poder explorar y moverse por un workspace de notas y archivos con agilidad.*
1. **Búsqueda y reemplazo en todo el Workspace (Global Search):**
   - Búsqueda de texto o regex en todos los archivos del proyecto en segundo plano.
   - Panel de resultados agrupados por archivo con vista previa de la línea.
   - Filtros de inclusión/exclusión (ignorar automáticamente `.git/`, `build/`, `node_modules/`, etc.).
   - Reemplazo global en lote con confirmación.
2. **Esquema de secciones y símbolos (Document Outline):**
   - Títulos de Markdown (`# H1`, `## H2`, `### H3`) y funciones/tipos de Go (`func`, `type`).
   - Salto instantáneo a la sección deseada.

### Fase 15 — Asistencia de edición y productividad ligera
*Objetivo: Escritura rápida sin interferir con el teclado del sistema.*
1. **Atajos táctiles opcionales:**
   - Barra de símbolos de acceso rápido sobre el teclado, configurable desde Ajustes.
2. **Navegación por secciones y búsqueda integrada:**
   - Salto rápido de archivos (`Quick Open` `Ctrl+P`).

### Fase 17 — Robustez móvil y formatos
*Objetivo: Seguridad de datos en el teléfono.*
1. **Recuperación automática de borradores (Draft Crash Recovery):**
   - Sistema de guardado en segundo plano de buffers modificados (autosave en almacenamiento temporal).
   - Restauración automática del texto no guardado en caso de que Android termine el proceso por falta de memoria o la app se cierre inesperadamente.
2. **Gestión de codificaciones (Encoding support):**
   - Lectura y escritura en codificaciones más allá de UTF-8 (UTF-16, ISO-8859-1, Windows-1252) con autodetección y cambio manual.

---

## 📊 Métricas de verificación de la base actual

| Hito | Tests JVM aprobados | Tests Instrumentados aprobados |
| --- | ---: | ---: |
| Fase 5 (Suite inicial) | 73 | — |
| Fase 6 (Canvas Renderer) | 84 | 5 |
| Fase 7 (IME & Teclado) | 99 | 16 |
| Fase 8 (Pestañas & Engine) | 107 | 19 |
| Fase 9 (SAF & Explorador) | 112 | 22 |
| Fase 10 (Productividad) | 129 | 25 |
| Fase 11 (Tokenizador & Decoraciones) | 137 | 28 |
| Fase 12-15 (Smart Edit, Multi-lenguaje, Búsqueda, Completion) | 189 | 54 |
| Estado actual del núcleo | 189 | 54 |

Comandos de verificación en Windows PowerShell:
```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:connectedDebugAndroidTest
```

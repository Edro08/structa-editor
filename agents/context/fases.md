# Fases de desarrollo de Structa

Las fases **1–11** representan el desarrollo del motor base y la arquitectura del editor (completadas). Las fases **12–18** definen la hoja de ruta técnica requerida para evolucionar Structa desde un editor de texto técnico hacia un **editor de código móvil completo y funcional**.

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

## 🚀 Hoja de ruta: Fases para ser un verdadero Editor de Código

Para que Structa pase de ser un visor/editor de texto a un **editor de código completo**, se definen las siguientes fases de desarrollo:

### Fase 12 — Ergonomía de edición de código (Smart Editing)
*Objetivo: Hacer que escribir código sea natural, rápido y no frustrante en móvil.*
1. **Pares de delimitadores automáticos (Auto-closing pairs):**
   - Cierre automático de `()`, `[]`, `{}`, `""`, `''` y triple comilla/backticks.
   - Si se escribe el carácter de cierre estando frente a él, saltar hacia adelante en lugar de duplicarlo.
   - Envolver selección con el delimitador si hay texto seleccionado.
   - Borrado en espejo: al presionar Backspace entre delimitadores vacíos `(|)`, borrar ambos caracteres.
2. **Indentación inteligente (Smart Auto-indent):**
   - Al presionar Enter tras abrir una llave `{` o dos puntos `:`, aumentar un nivel de indentación en la línea siguiente y posicionar el cierre con la sangría correcta.
   - Desindentación automática al escribir la llave de cierre `}`.
   - Configuración por proyecto/lenguaje: Indentación con espacios (2 o 4) o Tabs.
3. **Manipulación de líneas (Atajos de desarrollador):**
   - Comentar / descomentar línea o selección (`Ctrl+/` o menú de acción rápida).
   - Duplicar línea o selección (`Shift+Alt+Down` / `Shift+Alt+Up`).
   - Mover línea arriba / abajo (`Alt+Up` / `Alt+Down`).
   - Eliminar línea completa (`Ctrl+Shift+K` o equivalente táctil).
4. **Plegado de código (Code Folding):**
   - Detección de bloques plegables basados en sangría o delimitadores `{ ... }`.
   - Flechas/indicadores de plegado en el gutter junto al número de línea.
   - Ocultamiento de líneas plegadas en el renderizado del viewport.

### Fase 13 — Motor de lenguajes y sintaxis moderna
*Objetivo: Soporte robusto y coloreado de los principales lenguajes de programación.*
1. **Ampliación prioritaria de lenguajes:**
   - Incorporar soporte para **Kotlin, Java, Python, JavaScript, TypeScript, C/C++, Rust, HTML/CSS y Shell Script**.
2. **Integración de gramáticas estándar:**
   - Evaluar e integrar un motor de gramáticas probado (como TextMate grammars compiladas o Tree-sitter embebido en Android) para no mantener tokenizadores manuales frágiles por lenguaje.
3. **Detección avanzada de lenguaje:**
   - Reconocimiento por extensión, nombre especial (`Dockerfile`, `Makefile`, etc.) y shebang (`#!/usr/bin/env python3`).
   - Selector manual de sintaxis con persistencia por archivo.

### Fase 14 — Navegación de código y búsqueda global
*Objetivo: Poder explorar y moverse por una base de código completa.*
1. **Búsqueda y reemplazo en todo el Workspace (Global Search):**
   - Búsqueda de texto o regex en todos los archivos del proyecto en segundo plano.
   - Panel de resultados agrupados por archivo con vista previa de la línea.
   - Filtros de inclusión/exclusión (ignorar automáticamente `.git/`, `build/`, `node_modules/`, etc.).
   - Reemplazo global en lote con confirmación y transacción segura.
2. **Esquema de símbolos del documento (Document Outline):**
   - Extracción y visualización rápida de clases, funciones, métodos y constantes del archivo actual.
   - Diálogo rápido (`Ctrl+Shift+O` o icono en barra superior) para saltar a un símbolo.
3. **Marcas en la barra de desplazamiento (Scrollbar Markers):**
   - Vista general en la barra de scroll con indicadores de color: coincidencias de búsqueda, errores/advertencias y cambios de edición.

### Fase 15 — Asistencia de código y autocompletado (LSP)
*Objetivo: Convertir a Structa en una herramienta productiva con inteligencia de código.*
1. **UI de autocompletado adaptada a móvil:**
   - Menú emergente de sugerencias anclado al cursor que conviva fluidamente con el teclado virtual (IME).
   - Navegación táctil y por teclado de sugerencias.
2. **Completado básico por buffer:**
   - Extracción de palabras e identificadores de todos los documentos abiertos en el workspace (Word-based completion).
3. **Arquitectura de cliente LSP (Language Server Protocol):**
   - Capa de comunicación cliente LSP (vía socket o procesos locales en Android/Termux).
   - **Diagnósticos en tiempo real:** Subrayados ondulados (*squiggles*) en el Canvas para errores (rojo) y advertencias (amarillo), con tooltip táctil de explicación.
   - **Hover / Inspección:** Mostrar documentación o firma de tipo al mantener presionado un identificador.
   - **Navegación:** "Ir a definición" e "Ir a referencias".

### Fase 16 — Control de versiones (Git integrado)
*Objetivo: Controlar el ciclo de vida del código dentro de la aplicación.*
1. **Indicadores de estado en el Gutter:**
   - Barras de color junto al número de línea para marcar líneas añadidas (verde), modificadas (azul) o eliminadas (flecha roja).
2. **Panel de control de fuentes (Source Control):**
   - Visualización de archivos modificados, añadidos, borrados y en conflicto.
   - Integración con backend Git local (ej. libgit2 / JGit para Android).
3. **Visor de diferencias (Diff Viewer):**
   - Visualización lado a lado o unificada de cambios antes de confirmar.
4. **Operaciones básicas de Git:**
   - Stage / Unstage de archivos o bloques.
   - Commit con mensaje, creación y cambio de ramas (*checkout*), Pull y Push.

### Fase 17 — UX Móvil para desarrolladores y robustez
*Objetivo: Facilitar la programación desde un teléfono o tablet.*
1. **Barra de símbolos de programación (Accessory Bar móvil):**
   - Fila de accesos rápidos sobre el teclado virtual con caracteres de programación frecuentes: `{ } [ ] ( ) ; = : " ' / \ | & ! < > $ _ Tab`.
2. **Recuperación automática de borradores (Draft Crash Recovery):**
   - Sistema de guardado en segundo plano de buffers modificados (autosave en almacenamiento temporal).
   - Restauración automática del texto no guardado en caso de que Android termine el proceso por falta de memoria o la app se cierre inesperadamente.
3. **Gestión de codificaciones (Encoding support):**
   - Lectura y escritura en codificaciones más allá de UTF-8 (UTF-16, ISO-8859-1, Windows-1252) con autodetección y cambio manual.

### Fase 18 — Terminal integrada y ejecución (Opcional / Avanzado)
*Objetivo: Probar y ejecutar el código sin salir de Structa.*
1. **Terminal integrada:**
   - Emulador de terminal móvil embebido con soporte para shell local de Android o entorno Termux.
2. **Ejecución y runners:**
   - Disparo de scripts y comandos de construcción (`Run File`, `Build`, `Test`).

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
| Estado actual del núcleo | 167 | 54 |

Comandos de verificación en Windows PowerShell:
```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:connectedDebugAndroidTest
```

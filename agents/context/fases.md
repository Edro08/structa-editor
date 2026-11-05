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

### Fase 16 — Vista limpia renderizada de Markdown y Tablas GFM
- Parser de Markdown en Kotlin puro (`MarkdownParser`) sin librerías externas ni WebViews: encabezados (H1–H6), listas, citas, código cercado, formato inline anidado (código en línea dentro de negrita/cursiva/color) y soporte completo de tablas GFM con alineación de columnas.
- Visor `MarkdownViewer` en Jetpack Compose sobre Pure OLED con bloques de código copiables, tarjetas de tabla con cuadrícula continua 2D (`MarkdownTableLayout`), scroll horizontal fluido y selección libre de texto.
- Apertura en vista limpia por defecto para `.md` en modo lectura con alternancia rápida a código fuente o edición (`Ctrl+E`).

### Fase 17 — Codificaciones de texto (Charsets & Encodings)
- Detección automática inteligente de charset por inspección estricta de BOM y decodificador de secuencias UTF-8 con fallback seguro a Windows-1252.
- Soporte para 9 codificaciones: `UTF-8`, `UTF-8 BOM`, `UTF-16 LE`, `UTF-16 BE`, `ANSI (Windows-1252)`, `ISO-8859-1 (Latin 1)`, `US-ASCII`, `IBM437 (DOS)` y `Windows-1250`.
- Menú contextual y diálogo modal Pure OLED `EncodingSelectionDialog` para selección manual o autodetección.
- Abstracción de lectura/escritura (`readWithCharset`, `writeWithCharset`) en filesystem y casos de uso preservando el charset activo de cada documento.
- Indicador visual dinámico de codificación en el encabezado de información del documento.

### Fase 18 — Robustez móvil y persistencia (Draft Crash Recovery) & Estandarización Visual
- Dominio puro `domain.editor.draft`: `DocumentDraft` (documentId, content, timestamp, cursorAnchor, cursorActive, scrollX, scrollY, charset) y contrato `DraftRepository` con cero dependencias de Android.
- Implementación atómica `FileDraftRepository` en directorio privado (`files/drafts`): claves SHA-256, delimitador seguro y escritura temporal atómica para inmunidad contra cierres abruptos o falta de batería.
- Auto-guardado debounced (600ms) ante cambios en el buffer sucio y flush inmediato en ciclo de vida Android (`ON_STOP`, `onDispose` y `onCleared`).
- Detección automática y banner no intrusivo Pure OLED (`DraftRecoveryBanner`) con acciones de 1 toque: «Restaurar» (aplica contenido, marca dirty y restaura cursor/scroll) y «Descartar» (limpia borrador en disco).
- Notificación flotante inferior estilo Vaultk (`MessageOverlay`) con tarjeta en píldora (`20.dp`), acento verde esmeralda `#44DB98` para guardado y botón circular `X`.
- Estandarización de diálogos y modales: eliminación de colores arcoíris en badges, eliminación del notch en inputs, botones primarios violetas con disabled translúcido, botones secundarios `TextButton` planos y alturas con scroll fluido garantizado.
- Limpieza automática de borradores al guardar exitosamente el archivo o descartar cambios.

---

### Fase 19 — Ergonomía de Barra de Herramientas y Reorganización de Acciones (Secondary Action Bar / Toolbar)
- **Top Bar Principal Limpia y Despejada**:
  - Liberación del ancho total de la barra para el título del documento y línea de metadatos (`lenguaje · tamaño · charset · modo`), eliminando truncamientos abruptos (`BRUTE_FORCE_...`).
  - Eliminación de la acumulación de 4 botones apretados en la cabecera, reemplazados por un único botón toggle minimalista de solo icono (`Icons.Filled.Tune`).
- **Barra Secundaria Desplegable (`AnimatedVisibility`)**:
  - Despliegue fluido vertical con desvanecimiento (`expandVertically + fadeIn / shrinkVertically + fadeOut`) sobre contenedor Pure OLED oscuro (`surfaceContainer`) con borde sutil.
  - Scroll horizontal fluido para garantizar perfecta visualización en cualquier ancho de pantalla móvil.
  - Acciones directas de solo icono (1 toque):
    - **Modo**: Alternador rápido entre Lectura (lápiz) y Edición con iluminación violeta (`StructaPurple` + checkmark).
    - **Vista Previa**: Alternador contextual para Markdown/HTML entre vista formateada y código fuente crudo.
    - Notificaciones contextuales flotantes estilo Vaultk al accionar cada herramienta.
  - Menús contextuales temáticos y ordenados (4 a 6 opciones por menú, eliminando el menú vertical monolítico de 15 opciones):
    - **Archivo**: Abrir (`Ctrl+O`), Guardar (`Ctrl+S`), Guardar como..., Guardar todo, Cerrar y Cerrar otros/todos.
    - **Editar**: Deshacer (`Ctrl+Z`), Rehacer (`Ctrl+Y`), Buscar en archivo (`Ctrl+F`), Reemplazar (`Ctrl+H`) y Buscar en Workspace (`Ctrl+Shift+F`).
    - **Navegación**: Ir a línea (`Ctrl+G`), Esquema del documento / Outline (`Ctrl+Shift+O`), Ir al inicio e Ir al final.
    - **Más Opciones**: Formatear documento, Selector de lenguaje, Selector de codificación y Ajuste de línea (Word wrap toggle).

---

### Fase 20 — Rediseño de Colores para el Tema Claro (Snow & Slate) y Accesibilidad WCAG AAA
- **Erradicación de Cajas Negras en Markdown (`MarkdownViewer.kt`)**:
  - Reemplazo de bloques de código en negro azabache (`#18191E`) por contenedores Slate 100 (`#F1F5F9`) con borde Slate 200 (`#E2E8F0`), barra de herramientas en Slate 200 con etiqueta de lenguaje en Índigo 700 (`#4338CA`), texto en carbón nítido (`#0F172A`) y botón de copiado con contraste optimizado.
  - Corrección de tablas Markdown eliminando el fondo `#0E0F12` que causaba texto negro sobre fondo negro (contraste 1.1:1). Ahora renderiza tarjeta blanca pura con cabecera Slate 50, texto Slate 800 y celdas con tipografía accesible (WCAG AAA > 11:1).
  - Píldoras de código inline actualizadas de negro/cyan a contenedor Slate 200 con texto Índigo 700 semi-bold.
  - Enlaces Markdown convertidos a Azul técnico (`#2563EB`, ratio 6.5:1) y encabezados H3 a Índigo 700 (`#4338CA`).
- **Ajustes y Navegación (`SettingsScreen.kt`, `AppNavigation.kt`)**:
  - Encabezados de categoría (`APARIENCIA`, `EDITOR DE TEXTO`, `ALMACENAMIENTO`) en Índigo 700 (`#4338CA`) con tracking.
  - Iconos de preferencias con contenedor suave (`#EEF2FF`) y tinte Índigo (`#4F46E5`).
  - Enlace «Administrar permiso» e indicador de fuente con colores accesibles.
- **Tokens Semánticos Theme-Aware (`Theme.kt` & `Color.kt`)**:
  - `StructaCustomColors` ampliado con propiedades dedicadas para bloques de código, tablas, enlaces, código inline y encabezados, asegurando que el Modo Oscuro Pure OLED permanezca 100% inalterado y el Modo Claro ofrezca experiencia técnica premium.

### Fase 21 — Hardening de Seguridad, Integridad de Datos y Acción Compartir
- **Detección y Blindaje contra Archivos Binarios (`CharsetDetector.kt` & `DirectFileSystem.kt`)**:
  - Inspección de cabecera (primeros 8 KB) para detectar bytes nulos `0x00` y proporciones anómalas de caracteres de control (> 20%).
  - Rechazo automático de apertura de archivos binarios (`.png`, `.so`, `.zip`, `.pdf`, `.db`) con mensaje claro de protección, evitando el colapso de memoria y corrupción de datos al guardar.
- **Protección de Privacidad en Backups (`backup_rules.xml` & `data_extraction_rules.xml`)**:
  - Exclusión explícita del directorio privado de borradores (`domain="file" path="drafts"`), evitando que credenciales, tokens o notas confidenciales no guardadas se filtren en backups de Google en la nube.
- **Defensa en Profundidad de Red (`AndroidManifest.xml`)**:
  - Bloqueo explícito de tráfico no cifrado mediante `android:usesCleartextTraffic="false"`.
- **Blindaje del Filesystem contra Symlink Escape & Path Traversal (`DirectFileSystem.kt`)**:
  - Validación de frontera de directorio canónico (`target.parentFile?.canonicalPath == folder.canonicalPath`) en creación y renombrado.
  - Sanitización estricta de nombres prohibiendo secuencias `..` y caracteres de control invisibles (`< ' '`).
- **Acción «Compartir archivo» con FileProvider Seguro (`EditorScreen.kt` & `StructaApp.kt`)**:
  - Opción «Compartir…» en el menú desplegable `Archivo` que lanza un selector nativo de Android (`Intent.ACTION_SEND`).
  - `FileProvider` oficial configurado con permisos temporales de lectura para adjuntar el archivo físico o enviar su contenido de texto.

---

---

## 🟡 Hoja de ruta técnica

*(Fases futuras por planificar según requerimientos del producto)*

---

## 📊 Métricas de verificación

| Hito | Tests JVM aprobados | Tests Instrumentados aprobados |
| --- | ---: | ---: |
| Base inicial (Fases 1–11) | 137 | 28 |
| Ergonomía y búsqueda (Fases 12–14) | 189 | 54 |
| Modo lectura y Pure OLED (Fase 15) | 189 | 54 |
| Vista limpia Markdown (Fase 16) | 193 | 54 |
| Codificaciones y Tablas (Fase 17) | 207 | 54 |
| Persistencia y Borradores (Fase 18) | 221 | 54 |
| Ergonomía y Barra Secundaria (Fase 19) | 221 | 54 |
| Rediseño Tema Claro WCAG AAA (Fase 20) | 221 | 54 |
| Hardening y Compartir Archivo (Fase 21) | 225 | 54 |

Comandos de verificación en Windows PowerShell:
```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin
```

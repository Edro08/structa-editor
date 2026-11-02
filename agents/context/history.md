# Historial de Structa



## 2026-11-02 — Alineación Visual con Mockup Pure OLED: Botones, Badges y Navegación

- **Alineación de Botones Primarios con Degradado Profundo (Violet 600 a Indigo 600)**:
  - En `Theme.kt`, `DarkColors.primary` se configuró a `StructaPurple` (`0xFF6366F1`) y `onPrimary` a `Color.White`.
  - En `WorkspaceChrome.kt`, `WorkspacePrimaryButton` ("Abrir workspace") migró a un botón Material 3 con esquinas de 14.dp, fondo en degradado horizontal de Violeta a Índigo profundo (#7C3AED a #4F46E5), forma de píldora redondeada (24.dp) idéntica al mockup y tipografía blanca semi-bold.
  - En `EditorScreen.kt`, el estado vacío cuando no hay archivo abierto ("No hay un archivo abierto") se rediseñó con un hero centrado: badge de código en violeta/lila de 56dp, textos claros y botón primario Índigo "Ir a explorar" con icono `FolderOpen`.
- **Estilo de Badges e Iconos de Carpeta**:
  - `WorkspaceBadge` en Inicio y Recientes rediseñado con contenedor cuadrado de 40.dp, esquinas de 10.dp, fondo `StructaPurple` al 15% de opacidad, borde sutil al 30% y tint `StructaLavender`.
- **Espaciado y Estilo de Opciones de Búsqueda y Reemplazo (`EditorScreen`)**:
  - Se incrementó la separación entre chips (`Arrangement.spacedBy(8.dp)` en lugar de 5.dp) y el padding interno horizontal (12.dp) para evitar que `Aa`, `Palabra` y `Regex` se toquen o amontonen.
  - Se añadió relleno vertical (`vertical = 6.dp`) a la fila para que no quede pegada al campo de texto de arriba ni a la primera línea del editor de código abajo.
  - Los chips seleccionados ahora destacan con un contenedor `StructaPurple` al 22% de opacidad, borde al 60% y tipografía `StructaLavender` semi-bold.
  - Se ajustaron los botones "Reemplazar" y "Reemplazar todo" con separación adecuada y altura consistente de 32.dp.
- **Barra de Navegación Inferior Refinada**:
  - Fondo plano `DarkSurface` (`#0E0F12`) con divisor superior hairline sutil (`DarkOutlineVariant` con opacidad).
  - Píldora activa en `StructaPurple` al 20% de opacidad y esquinas redondeadas de 16.dp.
  - Texto e icono activo en `StructaLavender`; pestañas inactivas en `DarkTextMuted` (`#64748B`).
- **Verificación**:
  - Suite de pruebas unitarias aprobada (`:app:testDebugUnitTest`).
  - Suite de pruebas de instrumentación compilada (`:app:compileDebugAndroidTestKotlin`).
  - Aplicación reinstalada e iniciada en el emulador Pixel 9 Pro.

## 2026-11-02 — Tema Oscuro Pure OLED y Eliminación Definitiva de SAF



- **Adopción de Pure OLED como Tema Oscuro Principal**:

  - Fondo negro absoluto (`#000000`) para paneles OLED, reduciendo el consumo energético y maximizando el contraste tipográfico.

  - Elevación de superficies refinada: `DarkSurface` (`#0E0F12`), `DarkSurfaceRaised` (`#18191E`), con bordes hairline de baja opacidad (`#262B36`).

  - Acentos de alto contraste: Índigo eléctrico (`#6366F1`), Cyan técnico (`#38BDF8`), y Lila/Lavanda para navegación y breadcrumbs.

  - Canvas del editor y canalón de números (`StructaEditorView`) alineados al negro OLED (`#000000`).

- **Eliminación de Contenedores Card en Listas Principales**:

  - Pantalla de Inicio (`HomeScreen`): la lista de workspaces y recientes se dibuja plana directamente sobre el fondo con separadores sutiles, sin tarjetas envolventes.

  - Explorador de Archivos (`BrowserScreen`): se eliminó la tarjeta contenedora que rodeaba la lista de carpetas y archivos; los elementos se despliegan fluidamente ocupando todo el ancho y alto del canvas.

  - Se retiró por completo la píldora/banner de `"Acceso Completo (Directo) MANAGE_STORAGE"`.

- **Eliminación Total de Storage Access Framework (SAF)**:

  - Se eliminaron las clases y lectores de SAF: `SafFileSystem`, `SafFileReader`, `SafFileWriter`, `SafDirectoryReader`, `RoutedFileSystem` y `SafFileSystemTest`.

  - El sistema de archivos predeterminado y exclusivo pasa a ser `DirectFileSystem` con acceso POSIX nativo mediante `MANAGE_EXTERNAL_STORAGE`.

  - Simplificación en `SettingsRepository` y `AndroidSettingsRepository`: se eliminaron `fileAccessProvider()` y `setFileAccessProvider()`.

  - Simplificación en `SettingsScreen`: la sección de almacenamiento ahora muestra limpiamente el estado del permiso de acceso directo a archivos con el botón para gestionarlo o concederlo, eliminando la opción de alternar entre SAF y Direct.

  - En `StructaApp`: se eliminó el launcher `OpenDocumentTree()`; la apertura de carpetas invoca directamente el selector de directorios interno de Structa.

- **Mockup HTML Interactivo Actualizado**:

  - Actualizado [dark_theme_mockup.html](file:///C:/Users/Edro0/.gemini/antigravity/brain/1a9551d8-88b2-4000-b9c4-6c616808067c/dark_theme_mockup.html) con Pure OLED activo por defecto, listas planas en Inicio y Explorador sin tarjetas, sin banner de almacenamiento, y botones de cabecera de editor sin texto ni contorno.

- **Verificación**:

  - Suite completa de pruebas unitarias aprobada (`:app:testDebugUnitTest`).

  - Suite de pruebas de instrumentación compilada (`:app:compileDebugAndroidTestKotlin`).

  - Instalación exitosa en emulador (`Pixel 9 Pro - API 35`) y arranque limpio verificado en logcat.



## 2026-11-01 — Selección y Copia de Texto en Modo Lectura



- **Habilitación de selección de texto en modo lectura**:

  - Tanto el doble toque (`onDoubleTap`) como la pulsación larga (`onLongPress`) activan la selección de palabras (`selectWord`) en modo lectura (`editable = false`).

  - Arrastrar tras seleccionar (`dragAnchor`) expande fluidamente el rango seleccionado sin levantar el teclado virtual (`mInputShown = false`).

  - Un toque simple fuera de la selección (`onSingleTapUp`) limpia la selección activa (`ClearSelectionCommand`) y cierra el menú contextual flotante sin abrir el teclado.

- **Acciones contextuales y portapapeles (`performEditorContextAction`)**:

  - Menú flotante nativo (`ActionMode.TYPE_FLOATING` con `ActionMode.Callback2` y `onGetContentRect` ajustado al rectángulo del texto seleccionado).

  - En modo lectura se muestran y permiten exclusivamente acciones no destructivas: **"Copiar"** (`android.R.id.copy`) y **"Seleccionar todo"** (`android.R.id.selectAll`).

  - Acciones que modifican el contenido ("Cortar" y "Pegar") quedan bloqueadas y ocultas mientras se está en modo lectura.

  - Soporte de teclado físico para `Ctrl+C` y `Ctrl+A` en modo lectura.

- **Verificación**:

  - Test unitario agregado: `viewModeSupportsSelectionAndTextCopy` en `EditorDocumentsTest.kt`.

  - Pruebas unitarias aprobadas (`:app:testDebugUnitTest`).

  - Instalación y validación en emulador con teclado oculto (`mInputShown = false`).



## 2026-11-01 — Modo Lectura por Defecto y Eliminación de "Comentar Línea"



- **Modo lectura / visualización por defecto al abrir archivos**:

  - Todo archivo abierto (`EditorUiState.isEditable = false`) se abre ahora en **modo lectura / ver** como estado base.

  - Al estar en modo lectura:

    - No se abre el teclado en pantalla (Gboard) accidentalmente al hacer tap o scroll en el documento (`editable = false` en `StructaEditorView`).

    - El cursor parpadeante se oculta para una lectura limpia y sin distracciones (`cursorVisible = false`).

    - La barra de símbolos (`accessorySymbols`) se oculta automáticamente.

    - Se preserva la navegación completa: búsqueda en archivo, búsqueda global, índice de contenido (`Ctrl+Shift+O`), salto a línea e ir al inicio/final.

- **Alternar lectura y edición (`isEditable`)**:

  - Barra superior homogénea (TopBar): tanto **"Editar / Listo"** como **"Archivo"** se rediseñaron como `IconButton` limpios, sin texto y sin contorno, idénticos al botón de los tres puntos (⋮).

    - En modo lectura: ícono de lápiz (`Icons.Filled.Edit`).

    - En modo edición: ícono de verificación (`Icons.Filled.Check`).

    - Menú "Archivo": ícono de documento (`Icons.AutoMirrored.Filled.InsertDriveFile`).

  - Subtítulo en la TopBar que muestra el estado en vivo: `Go • 2.4 KB • Lectura` vs `Go • 2.4 KB • Edición`.

  - Opción en el menú de tres puntos (⋮): *"Habilitar edición"* / *"Cambiar a modo lectura"*.

  - Atajo de teclado físico: `Ctrl+E` (`EditorAction.TOGGLE_EDITABLE`).

  - Cada pestaña de documento recuerda su propio estado de edición de forma independiente.

- **Eliminación completa de "Comentar línea"**:

  - Se removió `TOGGLE_COMMENT` de `EditorAction`, `KeyBindingHandler.kt`, `SmartEditCommands.kt` y `EditorScreen.kt`.

  - Simplificación y limpieza de menús, alineada con la visión de Structa como editor de texto técnico ligero.

- **Verificación**:

  - Pruebas unitarias aprobadas (incluyendo nuevo test `documentOpensInViewModeByDefaultAndCanToggleEdit`).

  - Compilación e instalación de APK debug exitosa en emulador (`emulator-5554`).



## 2026-11-01 — Pivote: Editor de Texto Técnico y Archivos Flexibles (Solo Go para Código)



- **Ajuste de la hoja de ruta (`fases.md`)**:

  - Se eliminaron definitivamente las fases **16 (Control de versiones Git)** y **18 (Terminal integrada y ejecución)** para evitar sobrecarga y dependencias complejas.

  - El proyecto se reorienta oficialmente hacia un **editor de texto técnico y archivos flexibles** de alto rendimiento.

- **Simplificación del motor de lenguajes**:

  - Se retiraron todos los lenguajes de programación superfluos (Kotlin, Java, Python, JavaScript, TypeScript, C/C++, Rust, Shell, CSS, HTML).

  - Se mantiene únicamente **Go (`.go`)** como lenguaje de código para lectura/inspección visual.

  - Se conservan exclusivamente los formatos de texto y datos flexibles: **Markdown (`.md`)**, **Plain Text (`.txt`)**, **JSON (`.json`, `.jsonl`)**, **YAML (`.yaml`, `.yml`)** y **XML (`.xml`)**.

  - `DocumentSymbols.kt` optimizado para navegación real: se eliminó la extracción masiva de claves en JSON y YAML. Ahora se reserva exclusivamente como **Índice de contenido** para encabezados de **Markdown (`.md`)** y mapa de funciones/tipos de **Go (`.go`)**.

  - Renombrado en la interfaz de usuario: de *"Esquema de símbolos"* a **"Índice de contenido"** (`Ctrl+Shift+O`).

- **Verificación**:

  - 187 tests unitarios aprobados (`:app:testDebugUnitTest`).

  - Compilación y actualización del APK debug (`:app:assembleDebug`) sin errores.



## 2026-11-01 — Ajustes de Barra de Símbolos y Optimización de Esquema de Símbolos



- **Barra de símbolos configurable en Ajustes (`SettingsScreen`)**:

  - Se agregó una preferencia con interruptor (`Switch`) bajo la categoría `EDITOR DE CÓDIGO` en `SettingsScreen`.

- **Eliminación del predictor secundario en pantalla**:

  - Se eliminó por completo la fila flotante de chips de autocompletado en `EditorScreen.kt`, evitando redundancia con el teclado del sistema y recuperando espacio vertical limpio para edición.

  - Persistencia en preferencias compartidas (`show_accessory_bar`) a través de `SettingsRepository`, `AndroidSettingsRepository` y `SettingsViewModel`.

  - Conexión reactiva en `StructaApp.kt` y `EditorScreen.kt`: al desactivar el switch, la barra de accesos rápidos sobre el teclado se oculta por completo, maximizando el espacio de lectura y edición.

- **Optimización y protección de rendimiento para archivos pesados en Esquema de Símbolos**:

  - Para evitar bloqueos o congelamiento del hilo principal de Compose (ANR) en archivos grandes o pesados:

    - Se limitó la extracción léxica a un tope de seguridad de 1.000.000 de caracteres (~25.000 líneas) en `DocumentSymbolExtractor.kt`.

    - La extracción se ejecuta asíncronamente en una corrutina en segundo plano con `Dispatchers.Default` dentro de `EditorScreen.kt`.

    - Indicador visual de progreso (`loading`) en `DocumentOutlinePicker` mientras se calculan los símbolos.

- **Verificación**:

  - 189 tests unitarios aprobados (`:app:testDebugUnitTest`).

  - Compilación y verificación visual en emulador Android (`Pixel 9 Pro` / Android 15), confirmando encendido/apagado del switch y salto instantáneo a símbolos en el editor.



- **Fase 12 — Ergonomía de edición (Smart Editing Core)**:

  - `SmartEditCommands.kt`:

    - Inserción y sobre-escritura automática de delimitadores en pares: `()`, `[]`, `{}`, `""`, `''`, ````.

    - Envoltorio de selecciones activas al escribir un delimitador de apertura.

    - Borrado simétrico hacia atrás (`SmartDeleteBackwardCommand`) eliminando el par vacío completo.

    - Indentación inteligente automática al pulsar Enter tras llaves `{`, corchetes `[`, paréntesis `(` o dos puntos `:`, con centrado y sangría correcta al abrir bloque entre llaves `{}`.

    - Manipulación y atajos de línea:

      - `ToggleCommentCommand`: comentar / descomentar línea o selección (`Ctrl+/`).

      - `DuplicateLineCommand`: duplicar línea o selección arriba/abajo (`Shift+Alt+Up` / `Shift+Alt+Down` o `Ctrl+D`).

      - `MoveLineCommand`: mover línea arriba/abajo (`Alt+Up` / `Alt+Down`).

      - `DeleteLineCommand`: eliminar línea completa (`Ctrl+Shift+K`).

      - `IndentLinesCommand`: indentar y desindentar con espacios o tabs (`Tab` / `Shift+Tab`).

- **Fase 13 — Motor de lenguajes y sintaxis moderna (100% Kotlin Nativo)**:

  - Tokenizador léxico universal `CodeTokenizer` en `domain/editor/syntax/Syntax.kt` sin dependencias externas:

    - Soporte completo para **Kotlin, Java, Python, JavaScript, TypeScript, C / C++, Rust, Shell Script, CSS, HTML, XML, Markdown, Go, JSON y YAML**.

    - Soporte multilínea para comentarios de bloque (`/* */`), strings triples (`"""`, `'''`), template literals ('`') y anotaciones/decoradores (`@Annotation`).

    - Detección avanzada en `LanguageRegistry.forFileName(name, firstLine)`:

      - Nombres especiales: `Dockerfile`, `Makefile`, `GNUmakefile`, `CMakeLists.txt`.

      - Línea Shebang (`#!/usr/bin/env python3`, `#!/bin/bash`, `#!/usr/bin/env node`).

      - Selector manual y persistencia por archivo.

- **Fase 14 — Navegación de código y búsqueda global**:

  - `DocumentSymbolExtractor.kt`: Extracción léxica en tiempo real de clases, funciones, interfaces, métodos, tipos y títulos de Markdown por lenguaje, con diálogo `DocumentOutlinePicker` (`Ctrl+Shift+O` o menú) para salto directo a línea y offset.

  - `SearchWorkspace.kt` & `GlobalSearchViewModel.kt`: Motor de búsqueda y reemplazo en segundo plano en todo el workspace, con filtros automáticos de exclusión (`.git`, `build`, `node_modules`, `.gradle`), coincidencia de mayúsculas y reemplazo global en lote.

- **Fase 15 — Autocompletado nativo y barra de accesorios móvil**:

  - `CompletionEngine.kt`: Motor de autocompletado en Kotlin puro para palabras del buffer, identificadores y palabras clave del lenguaje activo con clasificación y ordenamiento de coincidencia exacta de prefijo.

  - Tira de chips de sugerencias interactivas por encima del teclado virtual para autocompletar identificadores con un toque.

  - Barra de símbolos de programación móvil con acceso rápido a caracteres esenciales: `{ } ( ) [ ] ; = : " ' / \ | & ! < > $ _ Tab`.

- **Verificación**:

  - 189 tests unitarios de JVM aprobados (`:app:testDebugUnitTest`).

  - Compilación de APK debug exitosa (`:app:assembleDebug`).

  - Instalación y validación funcional en emulador Android (`Pixel 9 Pro` / Android 15).





- **Rediseño final de `SettingsScreen.kt` (Propuesta A limpia)**:

  - Implementación del diseño nativo de *Preference Rows* sin tarjetas, adaptado a la retroalimentación del usuario:

    - **Retiro de elementos innecesarios**: Se eliminó la tira de vista previa de código (`fun main() { ... }`) y la sección redundante de "Acerca de" al pie.

    - **Categorías limpias**: `APARIENCIA`, `EDITOR DE CÓDIGO` y `ALMACENAMIENTO` con divisores finos de 0.5 dp.

    - **Controles compactos**: Selector de tema en cápsula `[ 🌙 Oscuro | ☀️ Claro ]`, selector de fuente directo `[ Sans | Mono ]` y stepper táctil de alta precisión para tamaño de letra `[ −  13 sp  + ]`.

- **Corrección de "Administrar permiso" en `StructaApp.kt`**:

  - Se corrigió el flujo de `requestDirect`: anteriormente, si `directAuthorized` ya era `true`, la función no realizaba ninguna acción.

  - Se introdujo `manageDirectPermission()` que invoca de forma infalible la pantalla de ajustes del sistema (`Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION` con fallbacks a `MANAGE_ALL_FILES_ACCESS_PERMISSION` y `APPLICATION_DETAILS_SETTINGS`), permitiendo al usuario conceder, revisar o revocar el permiso en cualquier momento.

- **Verificación**:

  - Compilación y ejecución exitosa de pruebas unitarias (`:app:testDebugUnitTest`).

  - Verificación funcional en emulador Android (`Pixel 9 Pro` / Android 15), confirmando apertura inmediata de la pantalla del sistema al pulsar "Administrar permiso".



## 2026-11-01 — Eliminación de Cards (Diseño Plano y Continuo en Inicio y Ajustes)



- **Eliminación de contenedores elevados (`WorkspacePanel` / cards)**:

  - **Pantalla de Inicio (`HomeScreen`)**:

    - Se removieron las tarjetas contenedoras de "Favoritos" y "Recientes", disponiendo los encabezados y filas de workspace directamente sobre el fondo general (`background`).

    - Se agregaron separadores sutiles de 0.5 dp (`HorizontalDivider`) entre elementos y secciones, logrando una estética plana, espaciosa y sin recuadros pesados.

  - **Pantalla de Ajustes (`SettingsScreen`)**:

    - Se eliminaron las 4 tarjetas flotantes ("Tema", "Editor", "Acceso a archivos", "Acerca de"), transformándolas en secciones planas continuas separadas por divisores finos.

    - Se preservaron íntegramente los controles internos (cápsula de tema, selector de tipografía, slider de tamaño, vista previa de código y filas de almacenamiento).

- **Consistencia visual global**:

  - Unificación estética total entre **Inicio**, **Explorador** y **Ajustes**, compartiendo una estructura plana, minimalista y continua alineada con la paleta Obsidian & Slate.

- **Verificación**:

  - Compilación e instalación en emulador Android (`Pixel_9_Pro` / Android 15).

  - Pruebas unitarias de JVM ejecutadas exitosamente (`:app:testDebugUnitTest`).



## 2026-11-01 — Refinamiento UI/UX de Inicio y Explorador



- **Pantalla de Inicio (`HomeScreen`)**:

  - Implementación de la Opción A: eliminación del doble empaquetado (*double-boxing*) y del borde blanco curvado superior que afectaba a las tarjetas anidadas.

  - Título y subtítulo ajustados en escala y peso tipográfico (`headlineSmall`, `FontWeight.Bold`, `bodySmall`) para coincidir exactamente con el mockup y la jerarquía compacta.

  - Listas de Favoritos y Recientes unificadas dentro de un único panel con divisores tenues de 0.5 dp (`HorizontalDivider`).

  - Optimización de espaciado y acento cromático `StatusOrange` en la estrella de favoritos activa.

- **Pantalla de Explorador (`BrowserScreen`)**:

  - **Rediseño completo de la interfaz según mockup**:

    - **Fila de controles y búsqueda unificada**: Buscador expandido y botones squircle de 40 dp con esquinas redondeadas de 14 dp.

    - **Botón `[ + ]` primario**: Destacado en color violeta vibrante (`StructaPurple` `#7C3AED`) con icono blanco, actuando como CTA principal de la barra de acciones.

    - **Breadcrumb estilizado**: Icono de carpeta lila (`StructaFolder` `#B084F4`), texto con peso medio y separadores chevron limpios.

    - **Iconografía semántica por tipo de archivo (`FileEntryIcon`)**:

      - Carpetas: icono de carpeta violeta sin caja pesada de fondo.

      - Archivos Kotlin (`build.gradle.kts`): icono geométrico oficial de Kotlin dibujado en `Canvas` con sus degradados (cian, violeta, naranja).

      - Archivos de código (`.kt`, `.py`, `.js`, etc.): squircle oscuro con icono de código `</>` en lila suave.

      - Archivos de texto y Markdown (`.md`, `.txt`): squircle oscuro con icono de documento.

    - **Optimización de jerarquía y altura de Workspace/Breadcrumb**:

      - Siguiendo las directrices de `ui-ux-pro-max` y `emil-design-eng`, se reemplazó el contenedor `WorkspacePanel` inflado por un contenedor de navegación esbelto y proporcionado de **36 dp** de altura (reducción del 50% frente a los ~70 dp anteriores).

      - Se eliminó el padding intrusivo de 48 dp impuesto por `TextButton`, sustituyéndolo por un layout interactivo centrado con `clickable`, icono de carpeta lila de 18 dp y separadores chevron sutiles.

    - **Rediseño fiel del Header superior de Explorador**:

      - **Fila de navegación y acciones compartidas**: A la izquierda, ruta breadcrumb (`📁 Workspace  ›  Editor`) con icono de carpeta en lavanda ([`StructaLavender`](file:///d:/Projects/Android/Structa/app/src/main/java/com/edro08/structa/ui/theme/Color.kt#L74) `#CAA8FF`) descansando limpiamente sobre el fondo sin contenedor ni bordes; a la derecha, botones de acción sin recuadro (`↻` de 24 dp y `+` de 26 dp) en tono lavanda a juego.

      - **Barra de búsqueda de ancho completo**: Campo de texto expandido horizontalmente (`fillMaxWidth`) de 44 dp con esquinas redondeadas de 14 dp, fondo `surface` y borde sutil al 40% de opacidad.

    - **Consolidación del explorador con lista plana y colores de archivo estándar**:

      - Regreso a la lista plana continua sin encapsulado forzado en tarjeta, apoyada directamente sobre el fondo con separadores finos de lado a lado (`HorizontalDivider`, 0.5 dp).

      - **Carpetas**: Icono limpio en amarillo cálido/dorado (`#F7C844`) estilo gestor nativo de archivos (Vaultk/ZArchiver), sin caja gris pesada.

      - **Badges predefinidos por tipo de archivo**: PDF en rojo (`#E53935`), ZIP en azul (`#2563EB`), APK en violeta (`#7C3AED`), Kotlin con logo oficial multicolor (`KotlinGradleLogo`), y documentos/código con iconos limpios.

      - **Conservación de los 3 puntos `⋮`**: Menú de acciones vertical a la derecha de cada elemento.

- **Pruebas y verificación**:

  - Compilación y ejecución exitosa de pruebas unitarias (`:app:testDebugUnitTest`).

  - Instalación y prueba funcional directa en el emulador (`Pixel 9 Pro` / Android 15), verificando el menú contextual de Agregar y la fluidez visual de ambas pantallas.



## 2026-11-01 — Centralización del sistema de temas y rediseño de Ajustes



- Centralización total de la capa de color en `ui/theme/Color.kt` y `ui/theme/Theme.kt` siguiendo la arquitectura de Vaultk:

  - Definición de tokens semánticos completos para Modo Oscuro (Obsidian & Slate: `#0E1015`, `#141720`, `#1F2330`, `#282D3D`) y Modo Claro (Snow & Slate: `#F8F9FB`, `#FFFFFF`, `#EFF2F6`).

  - Implementación de `@Immutable data class StructaCustomColors`, `LocalStructaCustomColors` y extensión `MaterialTheme.structaColors` / `MaterialTheme.structaCustomColors`.

  - Tokens centralizados para sintaxis de código (`codeKeyword`, `codeFunction`, `codeString`, `codeNumber`, `codeComment`, `codeOperator`) y estados (`statusSuccess`, `statusWarning`, `statusError`).

  - Eliminación de colores `Color(0x...)` hardcodeados en componentes de pantalla.

- Rediseño de la pantalla de Ajustes:

  - Selector de tema estilo cápsula segmentada neutra `[ 🌙 Oscuro | ☀️ Claro ]`.

  - Slider compacto de tamaño de fuente con rango 10–32 sp, badge monoespaciado y thumb circular.

  - Tarjeta de acceso a archivos compactada con lista limpia y descripción de una sola línea.

  - Corrección de navegación al pulsar sobre "Acceso completo" para permanecer en Ajustes sin saltar al selector de directorio.

  - Verificación en emulador físico (`Pixel_9_Pro` - Android 15) en temas claro y oscuro.



## 2026-11-01 — Reestructuración de especificaciones y hoja de ruta a Editor de Código



- Reorganización de fases: se desglosó la antigua fase 12 en una hoja de ruta técnica modular (Fases 12–18) en `fases.md` para convertir Structa en un editor de código completo (ergonomía de edición, gramáticas estándar de lenguajes, búsqueda global en workspace, autocompletado y LSP, Git integrado, UX móvil y terminal).

- Consolidación arquitectónica: se integró la especificación del Editor Core (`PieceTableBuffer`, `LineIndex`, `EditingSession`, `EditorEngine`, renderizado en `Canvas` y puente `IME`) dentro de `agents/specs/architecture.md`, eliminando `editor-core.md`.

- Normalización de la estructura y trazabilidad de documentación conforme a las instrucciones de `AGENTS.md`.



## 2026-10-06 — Proveedores de archivos



- Direct incorporó rutas absolutas de almacenamiento compartido junto a SAF; `RoutedFileSystem` conserva el editor independiente del proveedor. Hay selector Direct propio y permiso `MANAGE_EXTERNAL_STORAGE` mediante Ajustes.

- Se revirtió el prototipo de acceso a rutas restringidas: Configuración solo ofrece SAF y Direct. Direct sigue bloqueando `Android/data` y `Android/obb`; ese acceso queda fuera de Structa.

- Tras la limpieza pasaron 167 JVM y 54 instrumentadas; las dos pruebas Direct se repitieron con permiso temporal para ejercer operaciones reales y se devolvió el app-op a `default`.



## 2026-10-05 — Archivos grandes, apertura y lenguajes



- El resaltado de archivos grandes pasó a ventanas visibles con checkpoints de estado cada 128 líneas; los cambios invalidan los estados desde la primera línea modificada. `syncInput` conserva trabajo O(n) y un salto lejano puede reconstruir el estado desde el inicio.

- Android «Abrir con» incorporó URI externos `content://` y permisos de lectura/escritura según el proveedor; Guardar como permite exportar archivos de solo lectura.

- Se añadieron XML y JavaScript al selector/resaltado y formato conservador de XML/YAML. Después se retiró el resaltado de Kotlin, Java y JavaScript: quedan como texto plano; el formato vigente es JSON/YAML/XML.



## 2026-10-04 — Workspace, productividad y sintaxis



- Fase 9: filesystem SAF, explorador con operaciones y carga diferida; persistencia de pestañas/cursor/scroll y restauración leyendo disco.

- Fase 10: búsqueda/reemplazo con regex, Quick Open y paleta filtrable inicial. Posteriormente esa paleta se sustituyó por Archivo/Editar y `Ctrl+Shift+P` pasó a abrir Editar; el editor ganó altura al ocultar barras adicionales.

- Fase 11: resaltado léxico incremental y decoraciones; la fase 12 quedó pospuesta. Se quitó el bloqueo de edición por encima de 1 MB; permaneció el máximo de apertura de 25 MB. Se corrigió el Canvas que tapaba Buscar/Reemplazar.

- Configuración incorporó tema y estilo monoespaciado del editor; la navegación inferior dejó de mostrarse en la pantalla Editor.



## 2026-10-03 — Base del editor



- Fases 1–5: Piece Table, índice de líneas, cursor/selección, edición, comandos, Undo/Redo y pruebas del núcleo Kotlin puro. Se resolvieron bloqueos de tests previos antes de avanzar al renderer.

- Fases 6–7: Canvas con viewport y puente Compose; entrada táctil, teclado, IME/composición y portapapeles. Se corrigió el revelado del cursor al reducir la vista y se comprobaron transiciones a solo lectura y menú de pulsación larga.

- Fase 8: pestañas independientes, dirty por revisión y guardado asíncrono; cierre de documentos modificados con Guardar/Descartar/Cancelar.


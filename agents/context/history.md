# Historial de Structa

## 2026-11-01 — Rediseño Depurado de Ajustes y Corrección de Permisos

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

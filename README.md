# Structa

**Structa** es un editor de texto técnico y visor de archivos flexibles para Android, desarrollado en Kotlin y Jetpack Compose.

Diseñado para ofrecer una experiencia móvil ágil, limpia y enfocada, combina un motor de texto propio de alto rendimiento, visualización estructurada por defecto y acceso integral al sistema de archivos local.

---

## ✨ Características principales

- **Modo lectura por defecto**: los archivos se abren en modo de visualización limpio sin levantar el teclado accidentalmente, con selección de texto y copia al portapapeles fluidas.
- **Alternancia rápida de edición**: botón de un toque para habilitar o pausar la edición en cualquier momento (`Ctrl+E`).
- **Formatos flexibles soportados**:
  - **Texto y documentación**: Markdown (`.md`) y Texto plano (`.txt`).
  - **Archivos estructurados y de configuración**: JSON (`.json`, `.jsonl`), YAML (`.yaml`, `.yml`) y XML (`.xml`).
  - **Código técnico**: Go (`.go`) para inspección y lectura de código.
- **Navegación e índice de contenido**:
  - Índice de encabezados para Markdown y funciones/tipos para Go (`Ctrl+Shift+O`).
  - Búsqueda y reemplazo en el archivo actual.
  - Búsqueda global en el workspace (`Ctrl+Shift+F`).
  - Salto a línea (`Ctrl+G`) y navegación rápida al inicio/final.
- **Motor de editor propio**:
  - Buffer Piece Table optimizado para documentos ligeros y de gran volumen.
  - Renderizado nativo con desplazamiento bidireccional suave y ajuste de línea opcional (*word wrap*).
  - Historial de cambios completo (Deshacer / Rehacer).
  - Formateo de documentos JSON.
- **Gestión de documentos**:
  - Múltiples pestañas con retención de estado independiente.
  - Guardado regular, *Guardar como* y *Guardar todo*.
- **Atajos de teclado**: soporte completo para atajos de teclado físico estándar (`Ctrl+S`, `Ctrl+F`, `Ctrl+E`, `Ctrl+P`, etc.).
- **Explorador y workspaces**:
  - Acceso mediante Storage Access Framework (SAF).
  - Acceso directo al almacenamiento compartido (`MANAGE_EXTERNAL_STORAGE`).
  - Gestión de favoritos y archivos recientes.
- **Personalización y diseño**:
  - Interfaz adaptativa con Material You y soporte para tema claro y oscuro.
  - Ajuste de tipografía, tamaño de fuente y visibilidad de barra de símbolos.

---

## 📂 Acceso a archivos

Structa ofrece dos modalidades de acceso al almacenamiento:

### 1. Storage Access Framework (SAF)
Permite trabajar con carpetas y documentos seleccionados mediante el selector del sistema Android a través de URIs `content://`.

### 2. Acceso completo al almacenamiento
Utiliza acceso directo al sistema de archivos mediante el permiso `MANAGE_EXTERNAL_STORAGE`, permitiendo explorar rutas reales del dispositivo:

```text
/storage/emulated/0/Documents
/storage/emulated/0/Download
/storage/emulated/0/Projects
```

---

## 🎨 Interfaz de usuario (UI)

La interfaz está desarrollada íntegramente en Jetpack Compose, estructurada en 4 secciones principales:

- **Inicio**: accesos directos, carpetas favoritas y documentos recientes.
- **Explorador**: árbol de directorios con filtros, creación y gestión de archivos.
- **Editor**: visor y editor técnico con pestañas, barra de acciones plana y menú de comandos.
- **Ajustes**: personalización de fuente, tamaño, tema y preferencias del editor.

---

## 🚧 Estado del proyecto

Structa es un proyecto personal en desarrollo activo enfocado en simplicidad, rendimiento nativo y cero dependencias externas innecesarias.

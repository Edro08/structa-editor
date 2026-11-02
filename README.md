# Structa

Editor de texto técnico y visor de archivos para Android, enfocado en lectura, edición estructurada y acceso directo al almacenamiento local.

---

### ✨ Características principales

* **Modo lectura**: Apertura de archivos en solo lectura por defecto para evitar levantar el teclado accidentalmente. Permite seleccionar y copiar texto con normalidad.
* **Alternancia rápida**: Cambio inmediato entre lectura y edición mediante botón en la barra o atajo (`Ctrl+E`).
* **Navegación e índice**: Esquema de encabezados en Markdown y funciones/tipos en Go (`Ctrl+Shift+O`). Salto a línea (`Ctrl+G`).
* **Búsqueda y reemplazo**: Búsqueda en el archivo abierto con soporte para coincidencia exacta, palabras completas y expresiones regulares.
* **Búsqueda global**: Rastreo de texto en todos los archivos de la carpeta de trabajo (`Ctrl+Shift+F`).
* **Historial**: Deshacer y rehacer cambios ilimitado (`Ctrl+Z` / `Ctrl+Y`).
* **Atajos de teclado**: Soporte para teclados físicos estándar (`Ctrl+S`, `Ctrl+F`, `Ctrl+G`, `Ctrl+P`, etc.).

### 📄 Formatos soportados

* **Documentación**: Markdown (`.md`) y texto plano (`.txt`).
* **Configuración y datos**: JSON (`.json`, `.jsonl`), YAML (`.yaml`, `.yml`) y XML (`.xml`), con formateo automático para JSON.
* **Código técnico**: Go (`.go`) con extracción de símbolos y funciones.

### 📁 Archivos y workspaces

* **Acceso directo**: Apertura y edición sobre rutas reales del almacenamiento mediante `MANAGE_EXTERNAL_STORAGE`.
* **Explorador interno**: Árbol de directorios para navegar, renombrar, crear o eliminar archivos y carpetas sin selectores externos.
* **Pestañas múltiples**: Varios documentos abiertos en simultáneo con detección de cambios sin guardar.
* **Recientes y favoritos**: Accesos directos a carpetas y espacios de trabajo frecuentes desde la pantalla de inicio.

---

### ⚙️ Entorno

* **Sistema**: Android 12 (API 31) o superior.
* **Almacenamiento**: Acceso directo al sistema de archivos local (POSIX).
* **Interfaz**: Jetpack Compose con tema oscuro Pure OLED (negro `#000000`) y tema claro.

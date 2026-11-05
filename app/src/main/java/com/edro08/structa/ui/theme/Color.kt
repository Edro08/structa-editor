package com.edro08.structa.ui.theme

import androidx.compose.ui.graphics.Color

// --- PALETA OSCURA (Tema Oscuro: Pure OLED) ---
val DarkBackground = Color(0xFF000000)               // Negro absoluto para paneles OLED
val DarkSurface = Color(0xFF0E0F12)                  // Superficie sutil para top bars y diálogos
val DarkSurfaceRaised = Color(0xFF18191E)            // Subcontenedores / campos elevados
val DarkSurfaceContainerLowest = Color(0xFF000000)   // Fondo del editor (OLED black)
val DarkSurfaceContainerLow = Color(0xFF0A0B0E)      // Degradados sutiles / gutter
val DarkSurfaceContainer = Color(0xFF12141A)         // Contenedores estándar
val DarkSurfaceContainerHigh = Color(0xFF1B1E26)     // Contenedores secundarios
val DarkSurfaceContainerHighest = Color(0xFF242834)  // Píldoras y botones activos
val DarkOutline = Color(0xFF262B36)                  // Bordes interactivos
val DarkOutlineVariant = Color(0xFF181B22)           // Bordes sutiles hairline
val DarkTextPrimary = Color(0xFFF8FAFC)              // Texto principal (máximo contraste)
val DarkTextSecondary = Color(0xFF94A3B8)            // Texto secundario / atenuado
val DarkTextMuted = Color(0xFF64748B)                // Texto terciario / descripciones

// --- PALETA CLARA (Tema Claro: Snow & Slate) ---
val LightBackground = Color(0xFFF8F9FB)              // Fondo blanco nieve
val LightSurface = Color(0xFFFFFFFF)                 // Tarjetas blanco puro
val LightSurfaceRaised = Color(0xFFF1F3F7)           // Subcontenedores y campos
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)  // Fondo claro de código
val LightSurfaceContainerLow = Color(0xFFF6F8FA)     // Degradados de fondo claro
val LightSurfaceContainer = Color(0xFFEFF2F6)        // Contenedores estándar claros
val LightSurfaceContainerHigh = Color(0xFFE8ECF2)    // Contenedores secundarios claros
val LightSurfaceContainerHighest = Color(0xFFDDE3ED) // Píldoras activas
val LightOutline = Color(0xFFCBD2DE)                 // Bordes activos
val LightOutlineVariant = Color(0xFFE2E6EE)          // Bordes sutiles
val LightTextPrimary = Color(0xFF111827)             // Texto principal negro carbón
val LightTextSecondary = Color(0xFF4B5563)           // Texto secundario
val LightTextMuted = Color(0xFF9CA3AF)               // Texto terciario

// --- ACENTOS Y MARCA (Developer Palette) ---
val StructaPrimary = Color(0xFFF8FAFC)               // Acento neutro blanco alto contraste
val StructaPrimaryDark = Color(0xFF1E293B)           // Acento neutro oscuro para tema claro
val StructaSecondary = Color(0xFF94A3B8)             // Acento secundario pizarra
val StructaCyan = Color(0xFF38BDF8)                  // Cyan para símbolos y acciones clave
val StructaViolet = Color(0xFF7C3AED)                // Violeta profundo (Tailwind violet-600)
val StructaIndigo = Color(0xFF4F46E5)                // Índigo profundo (Tailwind indigo-600)
val StructaIndigoDeep = Color(0xFF4338CA)            // Índigo 700 para alto contraste en tema claro (WCAG AAA)
val StructaBlue = Color(0xFF2563EB)                  // Azul técnico para enlaces accesibles (WCAG AAA)
val StructaPurple = StructaIndigo                    // Acento primario más oscuro y saturado
val StructaFolder = Color(0xFF818CF8)                // Índigo 400 técnico para iconos y breadcrumbs
val StructaLavender = Color(0xFF818CF8)              // Índigo 400 con más cuerpo y saturación para tema oscuro

// --- ESTADOS DE FEEDBACK ---
val StatusGreen = Color(0xFF10B981)                  // Éxito / Permiso activo
val StatusOrange = Color(0xFFF59E0B)                 // Alerta / No guardado
val StatusRed = Color(0xFFEF4444)                    // Error

// --- SINTAXIS DE CÓDIGO (Tema Oscuro) ---
val CodeKeywordDark = Color(0xFFC792EA)              // fun, val, class, if
val CodeFunctionDark = Color(0xFF82AAFF)             // render, println
val CodeStringDark = Color(0xFFC3E88D)               // "texto"
val CodeNumberDark = Color(0xFFF78C6C)               // 123, 0xFF
val CodeCommentDark = Color(0xFF637777)              // // comentario
val CodeOperatorDark = Color(0xFF89DDFF)             // =, +, ->

// --- SINTAXIS DE CÓDIGO (Tema Claro) ---
val CodeKeywordLight = Color(0xFF7C3AED)
val CodeFunctionLight = Color(0xFF2563EB)
val CodeStringLight = Color(0xFF059669)
val CodeNumberLight = Color(0xFFD97706)
val CodeCommentLight = Color(0xFF6B7280)
val CodeOperatorLight = Color(0xFF0284C7)

// Aliases para retrocompatibilidad
val CodeKeyword = CodeKeywordDark
val CodeFunction = CodeFunctionDark
val CodeString = CodeStringDark
val CodeNumber = CodeNumberDark
val CodeComment = CodeCommentDark
val CodeOperator = CodeOperatorDark

val StructaBackground = DarkBackground
val StructaSurface = DarkSurface
val StructaSurfaceRaised = DarkSurfaceRaised
val StructaOutline = DarkOutline
val StructaMuted = DarkTextSecondary
val StructaInk = Color(0xFF171028)
val Purple80 = DarkTextPrimary
val PurpleGrey80 = DarkTextSecondary
val Pink80 = DarkOutline
val Purple40 = LightTextPrimary
val PurpleGrey40 = LightTextSecondary
val Pink40 = LightOutline

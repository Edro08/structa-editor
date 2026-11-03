package com.edro08.structa.domain.editor.markdown

import org.junit.Assert.*
import org.junit.Test

class MarkdownParserTest {

    @Test
    fun parseHeadings_recognizesLevelsAndSpans() {
        val md = """
            # Titulo 1
            ## Titulo 2
            ### <span style="color:orange">Preparar el entorno</span>
            ###### Titulo 6
        """.trimIndent()

        val nodes = MarkdownParser.parse(md)
        assertEquals(4, nodes.size)

        assertEquals(MarkdownNode.Heading(1, listOf(MarkdownSpan.Text("Titulo 1"))), nodes[0])
        assertEquals(MarkdownNode.Heading(2, listOf(MarkdownSpan.Text("Titulo 2"))), nodes[1])

        val h3 = nodes[2] as MarkdownNode.Heading
        assertEquals(3, h3.level)
        assertEquals(1, h3.spans.size)
        assertEquals(MarkdownSpan.Colored("Preparar el entorno", "orange"), h3.spans[0])

        assertEquals(MarkdownNode.Heading(6, listOf(MarkdownSpan.Text("Titulo 6"))), nodes[3])
    }

    @Test
    fun parseBoldItalicTripleAsterisks() {
        val md = "***Paso 1: Crear un contenedor:***"
        val nodes = MarkdownParser.parse(md)

        assertEquals(1, nodes.size)
        val p = nodes[0] as MarkdownNode.Paragraph
        assertEquals(1, p.spans.size)
        assertEquals(MarkdownSpan.BoldItalic("Paso 1: Crear un contenedor:"), p.spans[0])
    }

    @Test
    fun parseNestedInlineCodeInsideBold() {
        val text = "Haz clic en **`Realm settings`**."
        val spans = MarkdownParser.parseInline(text)

        assertEquals(3, spans.size)
        assertEquals(MarkdownSpan.Text("Haz clic en "), spans[0])
        val boldSpan = spans[1] as MarkdownSpan.Bold
        assertEquals(1, boldSpan.children.size)
        assertEquals(MarkdownSpan.InlineCode("Realm settings"), boldSpan.children[0])
        assertEquals(MarkdownSpan.Text("."), spans[2])
    }

    @Test
    fun parseNestedInlineCodeInsideBoldWithColon() {
        val text = "* **`Disabled`:** La deteccion esta apagada."
        val spans = MarkdownParser.parseInline(text)

        assertEquals(3, spans.size)
        assertEquals(MarkdownSpan.Text("* "), spans[0])
        val boldSpan = spans[1] as MarkdownSpan.Bold
        assertEquals(2, boldSpan.children.size)
        assertEquals(MarkdownSpan.InlineCode("Disabled"), boldSpan.children[0])
        assertEquals(MarkdownSpan.Text(":"), boldSpan.children[1])
        assertEquals(MarkdownSpan.Text(" La deteccion esta apagada."), spans[2])
    }

    @Test
    fun parseTable_headersAlignmentsAndRows() {
        val md = """
            | Parametro | Tipo | Descripcion |
            | :--- | :---: | ---: |
            | `id` | String | Identificador unico |
            | `name` | String | Nombre de **usuario** |
            | `enabled` | Boolean | Estado activo |
        """.trimIndent()

        val nodes = MarkdownParser.parse(md)
        assertEquals(1, nodes.size)
        assertTrue(nodes[0] is MarkdownNode.Table)

        val table = nodes[0] as MarkdownNode.Table
        assertEquals(3, table.headers.cells.size)
        assertEquals("Parametro", table.headers.cells[0].spans[0].text)
        assertEquals("Tipo", table.headers.cells[1].spans[0].text)
        assertEquals("Descripcion", table.headers.cells[2].spans[0].text)

        assertEquals(listOf(TableAlignment.LEFT, TableAlignment.CENTER, TableAlignment.RIGHT), table.alignments)

        assertEquals(3, table.rows.size)
        val row1 = table.rows[0]
        assertEquals(MarkdownSpan.InlineCode("id"), row1.cells[0].spans[0])
        assertEquals("String", row1.cells[1].spans[0].text)
        assertEquals("Identificador unico", row1.cells[2].spans[0].text)

        val row2 = table.rows[1]
        assertEquals("String", row2.cells[1].spans[0].text)
        assertEquals("Nombre de ", row2.cells[2].spans[0].text)
        assertEquals(MarkdownSpan.Bold("usuario"), row2.cells[2].spans[1])
    }

    @Test
    fun parseCodeBlock_extractsLanguageAndContent() {
        val md = """
            # Codigo
            ```go
            func main() {
                println("hello")
            }
            ```
            Texto posterior
        """.trimIndent()

        val nodes = MarkdownParser.parse(md)
        assertEquals(3, nodes.size)

        assertTrue(nodes[0] is MarkdownNode.Heading)
        val code = nodes[1] as MarkdownNode.CodeBlock
        assertEquals("go", code.language)
        assertEquals("func main() {\n    println(\"hello\")\n}", code.content)

        val paragraph = nodes[2] as MarkdownNode.Paragraph
        assertEquals("Texto posterior", paragraph.spans.first().text)
    }

    @Test
    fun parseLists_bulletAndNumbered() {
        val md = """
            - Primer item
            - Segundo item con **negrita**
              * Item anidado
            1. Paso uno
            2. Paso dos
        """.trimIndent()

        val nodes = MarkdownParser.parse(md)
        assertEquals(5, nodes.size)

        assertTrue(nodes[0] is MarkdownNode.BulletItem)
        assertEquals(0, (nodes[0] as MarkdownNode.BulletItem).depth)
        assertEquals("Primer item", (nodes[0] as MarkdownNode.BulletItem).spans[0].text)

        val item2 = nodes[1] as MarkdownNode.BulletItem
        assertEquals(0, item2.depth)
        assertEquals(2, item2.spans.size)
        assertEquals("Segundo item con ", item2.spans[0].text)
        assertEquals(MarkdownSpan.Bold("negrita"), item2.spans[1])

        val itemNested = nodes[2] as MarkdownNode.BulletItem
        assertEquals(1, itemNested.depth)
        assertEquals("Item anidado", itemNested.spans[0].text)

        val num1 = nodes[3] as MarkdownNode.NumberedItem
        assertEquals("1", num1.number)
        assertEquals("Paso uno", num1.spans[0].text)

        val num2 = nodes[4] as MarkdownNode.NumberedItem
        assertEquals("2", num2.number)
        assertEquals("Paso dos", num2.spans[0].text)
    }

    @Test
    fun parseThematicBreakAndBlockquotes() {
        val md = """
            > Esta es una cita con `codigo inline`
            ---
            Texto normal
        """.trimIndent()

        val nodes = MarkdownParser.parse(md)
        assertEquals(3, nodes.size)

        val quote = nodes[0] as MarkdownNode.Blockquote
        assertEquals("Esta es una cita con ", quote.spans[0].text)
        assertEquals(MarkdownSpan.InlineCode("codigo inline"), quote.spans[1])

        assertTrue(nodes[1] is MarkdownNode.ThematicBreak)
        assertTrue(nodes[2] is MarkdownNode.Paragraph)
    }

    @Test
    fun parseInline_supportsLinksFormattingAndHtmlSpans() {
        val text = "Visita [Structa](https://github.com/Edro08/Structa) para mas *detalles* o usa `Ctrl+S`. Y <span style=\"color:orange\">Naranja</span>."
        val spans = MarkdownParser.parseInline(text)

        assertEquals(9, spans.size)
        assertEquals(MarkdownSpan.Text("Visita "), spans[0])
        assertEquals(MarkdownSpan.Link("Structa", "https://github.com/Edro08/Structa"), spans[1])
        assertEquals(MarkdownSpan.Text(" para mas "), spans[2])
        assertEquals(MarkdownSpan.Italic("detalles"), spans[3])
        assertEquals(MarkdownSpan.Text(" o usa "), spans[4])
        assertEquals(MarkdownSpan.InlineCode("Ctrl+S"), spans[5])
        assertEquals(MarkdownSpan.Text(". Y "), spans[6])
        assertEquals(MarkdownSpan.Colored("Naranja", "orange"), spans[7])
        assertEquals(MarkdownSpan.Text("."), spans[8])
    }

    @Test
    fun parseEmptyString_returnsEmptyList() {
        assertTrue(MarkdownParser.parse("").isEmpty())
    }
}

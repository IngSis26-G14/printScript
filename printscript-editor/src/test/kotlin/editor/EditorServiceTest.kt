package editor

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditorServiceTest {
    @TempDir
    lateinit var directory: Path
    private val service = EditorService()

    @Test
    fun `extra space is highlighted and formatting is idempotent`() {
        val source = "let firstNumber: number =  6;\nprintln(firstNumber);\n"
        val result = service.analyze(source, "1.0", null, null)
        val issue = result.issues.single()
        assertTrue(issue.formatting)
        assertEquals("  ", source.substring(issue.start, issue.end))
        assertEquals("let firstNumber: number = 6;\nprintln(firstNumber);\n", result.formattedText)
        val clean = service.analyze(assertNotNull(result.formattedText), "1.0", null, null)
        assertTrue(clean.issues.isEmpty())
        assertEquals(result.formattedText, clean.formattedText)
    }

    @Test
    fun `lint diagnostic has exact identifier offsets`() {
        val source = "let bad_name: number = 6;"
        val result = service.analyze(source, "1.0", null, null)
        val issue = result.issues.single { !it.formatting }
        assertEquals("bad_name", source.substring(issue.start, issue.end))
        assertFalse(issue.error)
    }

    @Test
    fun `syntax and semantic errors block automatic formatting`() {
        for (source in listOf("let a: number = ;", "println(missing);")) {
            val result = service.analyze(source, "1.0", null, null)
            assertTrue(result.issues.any { it.error })
            assertNull(result.formattedText)
        }
    }

    @Test
    fun `explicit configs override defaults and report errors`() {
        val lint = directory.resolve("lint.json").apply { writeText("{}") }
        val format = directory.resolve("format.json").apply { writeText("{}") }
        val source = "let bad_name: number =  6;\n"
        assertTrue(service.analyze(source, "1.0", lint, format).issues.isEmpty())
        format.writeText("{ broken }")
        val broken = service.analyze(source, "1.0", lint, format)
        assertTrue(broken.issues.any { it.error })
        assertNull(broken.formattedText)
        assertTrue(service.analyze(source, "1.0", directory.resolve("missing.json"), format).issues.any { it.error })
    }

    @Test
    fun `configured println rule produces a warning`() {
        val config = directory.resolve("lint.json").apply {
            writeText("""{"mandatory-variable-or-literal-in-println": true}""")
        }
        val source = "println(1 + 2);"
        val warning = service.analyze(source, "1.0", config, null).issues.single()
        assertFalse(warning.error)
        assertEquals("1 + 2", source.substring(warning.start, warning.end))
    }

    @Test
    fun `version and runtime input are handled without executing source`() {
        val source = "let enabled: boolean = readInput(\"enabled?\");\nif (enabled) { println(\"yes\"); }"
        assertFalse(service.analyze(source, "1.1", null, null).issues.any { it.error })
        assertTrue(service.analyze(source, "1.0", null, null).issues.any { it.error })
    }

    @Test
    fun `strings unicode empty documents and EOF retain valid ranges`() {
        for (source in listOf("", "println(\"a  b 😀\");", "let x: number=6;", "let x: number =")) {
            val result = service.analyze(source, "1.0", null, null)
            result.issues.forEach {
                assertTrue(it.start in 0..source.length)
                assertTrue(it.end in it.start..source.length)
            }
            if (source.startsWith("println")) assertEquals(source, result.formattedText)
        }
    }

    @Test
    fun `multiple formatting changes have separate source ranges`() {
        val source = "let x: number=  6;\nlet y: number =  7;"
        val result = service.analyze(source, "1.0", null, null)
        assertEquals(3, result.issues.count { it.formatting })
        assertEquals("let x: number = 6;\nlet y: number = 7;", result.formattedText)
    }
}

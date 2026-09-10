package cli

import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliApplicationTest {
    @Test
    fun `validates a source file and reports parsing progress`() {
        val source = sourceFile("let value: number = 1;")
        val result = runCli("validation", source.toString())

        assertEquals(0, result.exitCode)
        assertContains(result.error, "Parsing:")
        assertContains(result.error, "100%")
    }

    @Test
    fun `reports syntax errors with their source range`() {
        val source = sourceFile("let value: number = 1")
        val result = runCli("validation", source.toString())

        assertEquals(1, result.exitCode)
        assertTrue(Regex("\\d+:\\d+-\\d+:\\d+").containsMatchIn(result.error))
        assertContains(result.error, "Expected ';'")
    }

    @Test
    fun `executes a valid source file`() {
        val source = sourceFile(
            """
            let value: number = 6 / 2;
            println("result: " + value);
            """.trimIndent(),
        )
        val result = runCli("execution", source.toString(), "--version", "1.0")

        assertEquals(0, result.exitCode)
        assertEquals("result: 3.0", result.output.trim())
    }

    @Test
    fun `reports interpreter semantic errors with their source range`() {
        val source = sourceFile(
            """
            let value: number;
            println(value);
            """.trimIndent(),
        )
        val result = runCli("execution", source.toString())

        assertEquals(1, result.exitCode)
        assertContains(result.error, "has not been initialized")
        assertTrue(Regex("\\d+:\\d+-\\d+:\\d+").containsMatchIn(result.error))
    }

    @Test
    fun `runs configured static analysis`() {
        val source = sourceFile("let snake_name: number = 1;")
        val config = configFile("{\"identifier_format\": \"camel case\"}")
        val result = runCli("analyzing", source.toString(), "--config", config.toString())

        assertEquals(1, result.exitCode)
        assertContains(result.error, "snake_name")
        assertTrue(Regex("\\d+:\\d+-\\d+:\\d+").containsMatchIn(result.error))
    }

    @Test
    fun `rejects a missing source file`() {
        val result = runCli("validation", "does-not-exist.ps")

        assertEquals(3, result.exitCode)
        assertContains(result.error, "does not exist")
    }

    @Test
    fun `passes selected version 1_1 to the language pipeline`() {
        val source = sourceFile("println(1);")
        val result = runCli("validation", source.toString(), "--version", "1.1")

        assertEquals(1, result.exitCode)
        assertContains(result.error, "Unsupported PrintScript version: 1.1")
    }

    @Test
    fun `explains that formatting is not yet available`() {
        val source = sourceFile("println(1);")
        val config = configFile("{}")
        val result = runCli("formatting", source.toString(), "--config", config.toString())

        assertEquals(4, result.exitCode)
        assertContains(result.error, "no formatter implementation")
    }

    private fun runCli(vararg arguments: String): CliResult {
        val outputBytes = ByteArrayOutputStream()
        val errorBytes = ByteArrayOutputStream()
        val application = CliApplication(
            standardOutput = PrintStream(outputBytes),
            standardError = PrintStream(errorBytes),
            standardInput = BufferedReader(StringReader("")),
            environment = emptyMap(),
        )

        val exitCode = application.run(arrayOf(*arguments))
        return CliResult(exitCode, outputBytes.toString(), errorBytes.toString())
    }

    private fun sourceFile(content: String): Path {
        val path = createTempFile(suffix = ".ps")
        Files.writeString(path, content)
        return path
    }

    private fun configFile(content: String): Path {
        val path = createTempFile(suffix = ".json")
        Files.writeString(path, content)
        return path
    }

    private data class CliResult(
        val exitCode: Int,
        val output: String,
        val error: String,
    )
}

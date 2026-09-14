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
        val source = sourceFile("const enabled: boolean = true; if (enabled) { println(readEnv('NAME')); }")
        val result = runCli("validation", source.toString(), "--version", "1.1")

        assertEquals(0, result.exitCode)
        assertContains(result.error, "100%")
    }

    @Test
    fun `rejects version 1_1 syntax when version 1_0 is selected`() {
        val source = sourceFile("const enabled: boolean = true;")
        val result = runCli("validation", source.toString(), "--version", "1.0")

        assertEquals(1, result.exitCode)
    }

    @Test
    fun `formats source with exact newlines and reports parsing progress`() {
        val source = sourceFile("let x:number=1;println(x+2);")
        val config = configFile("{}")
        val result = runCli("formatting", source.toString(), "--config", config.toString())

        assertEquals(0, result.exitCode, result.error)
        assertEquals("let x: number = 1;\nprintln(x + 2);\n", result.output)
        assertContains(result.error, "Parsing:")
        assertEquals("let x:number=1;println(x+2);", Files.readString(source))
    }

    @Test
    fun `formats version 1_1 blocks using JSON options`() {
        val source = sourceFile("if(flag){println(1);}else{println(2);}")
        val config = configFile("{\"indent-inside-if\":2,\"line-breaks-before-println\":1}")
        val result = runCli("formatting", source.toString(), "-v", "1.1", "-c", config.toString())
        assertEquals(0, result.exitCode, result.error)
        assertEquals("if (flag) {\n\n  println(1);\n} else {\n\n  println(2);\n}\n", result.output)
    }

    @Test
    fun `formatting reports syntax and configuration errors`() {
        val source = sourceFile("println(1)")
        val config = configFile("{}")
        val syntax = runCli("formatting", source.toString(), "-c", config.toString())
        assertEquals(1, syntax.exitCode)
        assertContains(syntax.error, "Expected ';'")
        assertEquals("", syntax.output)
        Files.writeString(config, "{\"line-breaks-before-println\":3}")
        val configuration = runCli("formatting", source.toString(), "-c", config.toString())
        assertEquals(1, configuration.exitCode)
        assertContains(configuration.error, "must be 0, 1, or 2")
    }

    @Test
    fun `executes version 1_1 with standard input and environment`() {
        val source = sourceFile(
            "const flag: boolean = true; let n: number = readInput('Number'); " +
                "if (flag) { println(n); println(readEnv('NAME')); }",
        )
        val result = runCli(
            "execution",
            source.toString(),
            "--version",
            "1.1",
            input = "12\n",
            environment = mapOf("NAME" to "Ada"),
        )
        assertEquals(0, result.exitCode, result.error)
        assertEquals("Number\n12\nAda\n", result.output)
    }

    @Test
    fun `conditional validation requires a declared boolean variable`() {
        val invalidPrograms = listOf(
            "let flag: number = 1; if (flag) {}",
            "let flag: string = 'true'; if (flag) {}",
            "let flag: number = readInput('number'); if (flag) {}",
            "let flag: boolean; if (flag) {}",
            "if (missing) {}",
            "if (true) {}",
            "if (readInput('flag')) {}",
        )
        for (program in invalidPrograms) {
            val result = runCli("validation", sourceFile(program).toString(), "--version", "1.1")
            assertEquals(1, result.exitCode, program)
            assertTrue(Regex("\\d+:\\d+-\\d+:\\d+").containsMatchIn(result.error), result.error)
        }
    }

    @Test
    fun `conditional validation accepts boolean variables from runtime reads`() {
        for (initializer in listOf("true", "readInput('flag')", "readEnv('FLAG')")) {
            val program = "let flag: boolean = $initializer; if (flag) {} else { if (flag) {} }"
            val result = runCli("validation", sourceFile(program).toString(), "--version", "1.1")
            assertEquals(0, result.exitCode, result.error)
        }
    }

    private fun runCli(
        vararg arguments: String,
        input: String = "",
        environment: Map<String, String> = emptyMap(),
    ): CliResult {
        val outputBytes = ByteArrayOutputStream()
        val errorBytes = ByteArrayOutputStream()
        val application = CliApplication(
            standardOutput = PrintStream(outputBytes),
            standardError = PrintStream(errorBytes),
            standardInput = BufferedReader(StringReader(input)),
            environment = environment,
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

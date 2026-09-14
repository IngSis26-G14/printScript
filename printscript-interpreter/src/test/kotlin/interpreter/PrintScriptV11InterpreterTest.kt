package interpreter

import common.io.reader.env.EnvReader
import common.io.reader.input.InputReader
import common.io.writer.OutputWriter
import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.model.value.StringValue
import common.model.value.Value
import common.type.option.Option
import common.type.outcome.Outcome
import lexer.PrintScriptLexer
import parser.PrintScriptParser
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintScriptV11InterpreterTest {
    @Test
    fun `booleans and constants execute and constants cannot be reassigned`() {
        val result = run("const flag: boolean = true; flag = false; println(flag);")
        assertEquals(listOf("true"), result.output)
        assertContains(result.diagnostics.single().message, "constant")
    }

    @Test
    fun `executes only selected branches and updates variables`() {
        val source = """
            let flag: boolean = true;
            let x: number = 0;
            if (flag) { x = 1; } else { x = 2; }
            flag = false;
            if (flag) { println(readInput("unused")); } else { x = 3; }
            if (flag) { println("unused"); }
            println(x);
        """.trimIndent()
        assertSuccess(run(source), listOf("3"))
    }

    @Test
    fun `executes nested conditional blocks`() {
        val source = "let flag: boolean = true; if (flag) { if (flag) { println('nested'); } }"
        assertSuccess(run(source), listOf("nested"))
    }

    @Test
    fun `converts input to destination types including reassignment and parentheses`() {
        val source = """
            let n: number = readInput("number");
            n = (readInput("again"));
            let flag: boolean = readInput("boolean");
            let text: string = readInput("string");
            println(n); println(flag); println(text);
        """.trimIndent()
        assertSuccess(
            run(source, listOf("2", "3.5", "false", "  hello  ")),
            listOf("number", "again", "boolean", "string", "3.5", "false", "  hello  "),
        )
    }

    @Test
    fun `converts environment values and reads strings directly in println`() {
        val source = """
            const n: number = readEnv("N");
            let flag: boolean = readEnv("FLAG");
            println(n); println(flag); println(readEnv("N"));
            println(readInput("prompt"));
        """.trimIndent()
        assertSuccess(
            run(source, listOf("0042"), mapOf("N" to "0012", "FLAG" to "true")),
            listOf("12", "true", "0012", "prompt", "0042"),
        )
    }

    @Test
    fun `read arguments may be string expressions`() {
        val source = "let prefix: string = 'your '; let n: number = readInput(prefix + 'age'); println(n);"
        assertSuccess(run(source, listOf("7")), listOf("your age", "7"))
    }

    @Test
    fun `invalid conversions stop execution with a source location`() {
        for ((type, value) in listOf("boolean" to "Hola", "number" to "abc", "number" to "NaN")) {
            val source = "let x: $type = readInput('prompt'); println('must not run');"
            val result = run(source, listOf(value))
            assertEquals(listOf("prompt"), result.output)
            assertContains(result.diagnostics.single().message, "Cannot convert")
            assertContains(result.diagnostics.single().format(), "1:")
        }
        val result = run("let x: boolean = readEnv('FLAG'); println('must not run');", env = mapOf("FLAG" to "Hola"))
        assertTrue(result.output.isEmpty())
        assertContains(result.diagnostics.single().message, "Cannot convert")
    }

    @Test
    fun `missing environment values stop execution`() {
        val result = run("println(readEnv('MISSING')); println('must not run');")
        assertTrue(result.output.isEmpty())
        assertContains(result.diagnostics.single().message, "not defined")
    }

    @Test
    fun `read arguments must be strings and condition must be boolean`() {
        for (source in listOf("println(readInput(12));", "println(readEnv(true));")) {
            val result = run(source)
            assertTrue(result.output.isEmpty())
            assertContains(result.diagnostics.single().message, "must be a string")
        }
        val result = run("let n: number = 1; if (n) { println('bad'); }")
        assertTrue(result.output.isEmpty())
        assertContains(result.diagnostics.single().message, "must be a boolean")
    }

    @Test
    fun `version 1_0 rejects version 1_1 ASTs before side effects`() {
        val result = run("println(readInput('prompt'));", version = "1.0")
        assertTrue(result.output.isEmpty())
        assertContains(result.diagnostics.single().message, "requires PrintScript 1.1")
    }

    private fun assertSuccess(result: Result, expected: List<String>) {
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.joinToString { it.format() })
        assertEquals(expected, result.output)
    }

    private fun run(
        source: String,
        lines: List<String> = emptyList(),
        env: Map<String, String> = emptyMap(),
        version: String = "1.1",
    ): Result {
        val tokens = PrintScriptLexer().lex("1.1", source.asSequence()).map {
            when (it) {
                is Outcome.Ok -> it.value
                is Outcome.Error -> error(it.error.format())
            }
        }
        val nodes: Sequence<Node> = PrintScriptParser().parse("1.1", tokens).map {
            when (it) {
                is Outcome.Ok -> it.value
                is Outcome.Error -> error(it.error.format())
            }
        }
        val output = mutableListOf<String>()
        val remaining = lines.iterator()
        val inputReader = object : InputReader {
            override fun read(): Sequence<Char> {
                check(remaining.hasNext()) { "Unexpected input read" }
                return remaining.next().asSequence()
            }
        }
        val writer = object : OutputWriter {
            override fun write(input: Sequence<String>) {
                output.addAll(input)
            }
        }
        val envReader = object : EnvReader {
            override fun read(key: String): Option<Value> =
                env[key]?.let { Option.Some(StringValue(it)) } ?: Option.None
        }
        val diagnostics = PrintScriptInterpreter().interpret(version, nodes, inputReader, writer, envReader).toList()
        return Result(output, diagnostics)
    }

    private data class Result(val output: List<String>, val diagnostics: List<Diagnostic>)
}

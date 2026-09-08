package interpreter

import common.io.reader.env.EnvReader
import common.io.reader.input.InputReader
import common.io.writer.OutputWriter
import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.model.value.Value
import common.type.option.Option
import common.type.outcome.Outcome
import lexer.PrintScriptLexer
import parser.PrintScriptParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintScriptInterpreterTest {

    @Test
    fun `interprets a variable value`() {
        val output = RecordingOutput()
        val diagnostics = interpret(
            """
            let x: number = 2;
            println("value: " + x);
            """.trimIndent(),
            output,
        )

        assertTrue(diagnostics.isEmpty())
        assertEquals(listOf("value: 2"), output.values)
    }

    @Test
    fun `interprets declarations assignments arithmetic and output`() {
        val output = RecordingOutput()
        val diagnostics = interpret(
            """
            let x: number = 2;
            let y: number = 3;
            let result: number = x + y * 4;
            println("x: " + x);
            println("y: " + y);
            println("result: " + result);
            """.trimIndent(),
            output,
        )

        assertTrue(diagnostics.isEmpty())
        assertEquals(listOf("x: 2", "y: 3", "result: 14"), output.values)
    }

    @Test
    fun `supports reassignment and floating point division`() {
        val output = RecordingOutput()
        val diagnostics = interpret(
            """
            let value: number;
            value = 5 / 2;
            println(value);
            """.trimIndent(),
            output,
        )

        assertTrue(diagnostics.isEmpty())
        assertEquals(listOf("2.5"), output.values)
    }

    @Test
    fun `reports semantic errors without mutating invalid declarations`() {
        val output = RecordingOutput()
        val diagnostics = interpret(
            """
            let name: string = "Ada";
            let name: string = "Grace";
            name = 42;
            println(name);
            """.trimIndent(),
            output,
        )

        assertEquals(2, diagnostics.size)
        assertTrue(diagnostics[0].message.contains("already declared"))
        assertTrue(diagnostics[1].message.contains("Cannot assign"))
        assertEquals(listOf("Ada"), output.values)
    }

    @Test
    fun `reports unsupported versions before consuming nodes`() {
        val output = RecordingOutput()
        val diagnostics = PrintScriptInterpreter().interpret(
            "2.0",
            emptySequence(),
            EmptyInput,
            output,
            EmptyEnv,
        ).toList()

        assertEquals(1, diagnostics.size)
        assertEquals("Configuration", diagnostics.single().category.name)
    }

    private fun interpret(source: String, output: RecordingOutput): List<Diagnostic> {
        val tokenResults = PrintScriptLexer().lex("1.0", source.asSequence()).toList()
        val tokens = tokenResults.map {
            when (it) {
                is Outcome.Ok -> it.value
                is Outcome.Error -> error(it.error.format())
            }
        }
        val nodeResults = PrintScriptParser().parse("1.0", tokens.asSequence()).toList()
        val nodes: List<Node> = nodeResults.map {
            when (it) {
                is Outcome.Ok -> it.value
                is Outcome.Error -> error(it.error.format())
            }
        }
        return PrintScriptInterpreter().interpret(
            "1.0",
            nodes.asSequence(),
            EmptyInput,
            output,
            EmptyEnv,
        ).toList()
    }

    private object EmptyInput : InputReader {
        override fun read(): Sequence<Char> = emptySequence()
    }

    private object EmptyEnv : EnvReader {
        override fun read(key: String): Option<Value> = Option.None
    }

    private class RecordingOutput : OutputWriter {
        val values = mutableListOf<String>()

        override fun write(input: Sequence<String>) {
            input.forEach {
                values.add(it)
                println(it)
            }
        }
    }
}

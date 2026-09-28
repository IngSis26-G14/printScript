package validator

import common.model.diagnostic.Diagnostic
import common.type.outcome.Outcome
import lexer.PrintScriptLexer
import parser.PrintScriptParser
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintScriptValidatorTest {
    @Test
    fun `read arguments reject known non string runtime values`() {
        for (function in listOf("readInput", "readEnv")) {
            for (type in listOf("number", "boolean")) {
                val errors = validate("let x: $type = readInput('value'); println($function(x));")
                assertContains(errors.single().message, "argument must be a string")
                assertContains(errors.single().format(), "1:")
            }
        }
    }

    @Test
    fun `read arguments accept literals string variables and string expressions`() {
        for (function in listOf("readInput", "readEnv")) {
            for (argument in listOf("'text'", "prompt", "prompt + ' suffix'")) {
                val errors = validate("let prompt: string = readInput('prompt'); println($function($argument));")
                assertTrue(errors.isEmpty(), errors.toString())
            }
        }
    }

    @Test
    fun `uninitialized operand reaches binary type validation`() {
        val errors = validate("let x: number;\nprintln(x + 5);")
        assertEquals(1, errors.size)
        assertEquals(
            "Cannot apply operator '+' to operands of type none and number",
            errors.single().message,
        )
    }

    private fun validate(source: String): List<Diagnostic> {
        val tokens = PrintScriptLexer().lex("1.1", source.asSequence()).map {
            when (it) {
                is Outcome.Ok -> it.value
                is Outcome.Error -> error(it.error.format())
            }
        }
        val nodes = PrintScriptParser().parse("1.1", tokens).map {
            when (it) {
                is Outcome.Ok -> it.value
                is Outcome.Error -> error(it.error.format())
            }
        }
        return PrintScriptValidator().validate("1.1", nodes).filterIsInstance<Outcome.Error<Diagnostic>>().map { it.error }.toList()
    }
}

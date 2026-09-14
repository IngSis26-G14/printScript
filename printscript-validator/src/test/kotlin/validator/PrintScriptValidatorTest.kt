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
    fun `uninitialized variables fail wherever a value is required`() {
        for (statement in listOf("println(x);", "println(readInput(x));", "println(x + 'suffix');", "let y:string=x;")) {
            val errors = validate("let x: string; $statement")
            assertContains(errors.single().message, "has not been initialized")
        }
        assertTrue(validate("let x: string; x = 'ready'; println(x);").isEmpty())
        assertTrue(validate("let unused: string;").isEmpty())
    }

    @Test
    fun `uninitialized diagnostic covers the identifier`() {
        val errors = validate("let x: number;\nprintln(x);")
        assertEquals(1, errors.size)
        assertContains(errors.single().format(), "2:9-2:10")
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

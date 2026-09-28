import common.model.diagnostic.Diagnostic
import common.model.node.BlockNode
import common.model.node.ElseBlockNode
import common.model.node.IdentifierNode
import common.model.node.IfStatementNode
import common.model.node.Node
import common.type.outcome.Outcome
import lexer.PrintScriptLexer
import parser.PrintScriptParser
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ConditionalGrammarTest {
    @Test
    fun `accepts a variable condition with both branches`() {
        val result = parse("if (flag) { println(1); } else { println(2); }").single()
        val node = assertIs<Node.Composite>(assertIs<Outcome.Ok<Node>>(result).value)
        assertEquals(IfStatementNode, node.type)
        assertEquals(IdentifierNode, node.children.elementAt(2).type)
        assertEquals(BlockNode, node.children.elementAt(4).type)
        assertEquals(ElseBlockNode, node.children.elementAt(5).type)
    }

    @Test
    fun `accepts empty blocks and nested if inside else braces`() {
        for (source in listOf("if (flag) {}", "if (flag) {} else {}", "if (flag) {} else { if (other) {} }")) {
            assertIs<Outcome.Ok<Node>>(parse(source).single(), source)
        }
    }

    @Test
    fun `rejects literals calls and expressions as conditions`() {
        for (condition in listOf("true", "false", "1", "'text'", "readInput('prompt')", "readEnv('FLAG')", "flag + 1", "(flag)", "")) {
            val results = parse("if ($condition) {}")
            assertIs<Outcome.Error<Diagnostic>>(results.single(), condition)
        }
    }

    @Test
    fun `requires opening and closing braces for both branches`() {
        val cases = mapOf(
            "if (flag) 1" to "Expected '{'",
            "if (flag) println(1);" to "Expected '{'",
            "if (flag)" to "Expected '{'",
            "if (flag) {" to "Expected '}'",
            "if (flag) { println(1);" to "Expected '}'",
            "if (flag) {} else 1" to "Expected '{'",
            "if (flag) {} else println(1);" to "Expected '{'",
            "if (flag) {} else" to "Expected '{'",
            "if (flag) {} else {" to "Expected '}'",
        )
        for ((source, message) in cases) {
            val errors = parse(source).filterIsInstance<Outcome.Error<Diagnostic>>()
            assertEquals(1, errors.size, source)
            assertContains(errors.single().error.message, message)
        }
    }

    @Test
    fun `rejects else if and duplicate else`() {
        for (source in listOf("if (flag) {} else if (other) {}", "if (flag) {} else {} else {}")) {
            assertTrue(parse(source).any { it is Outcome.Error }, source)
        }
    }

    @Test
    fun `diagnostic identifies the invalid condition token`() {
        val error = assertIs<Outcome.Error<Diagnostic>>(parse("if (true) {}").single()).error
        assertContains(error.format(), "1:5-1:9")
        assertContains(error.message, "Expected identifier")
    }

    private fun parse(source: String): List<Outcome<Node, Diagnostic>> {
        val tokens = PrintScriptLexer().lex("1.1", source.asSequence()).map {
            when (it) {
                is Outcome.Ok -> it.value
                is Outcome.Error -> error(it.error.format())
            }
        }
        return PrintScriptParser().parse("1.1", tokens).toList()
    }
}

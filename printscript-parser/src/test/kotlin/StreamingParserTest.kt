import common.model.diagnostic.Diagnostic
import common.model.node.IfStatementNode
import common.model.node.LetDeclarationStatementNode
import common.model.node.Node
import common.model.node.PrintlnStatementNode
import common.model.span.Position
import common.model.span.Span
import common.type.outcome.Outcome
import lexer.PrintScriptLexer
import parser.PrintScriptParser
import parser.internal.model.error.ParseError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StreamingParserTest {
    @Test
    fun `parses an expression larger than the former lookahead limit`() {
        val expression = (1..100).joinToString(" + ")
        val result = parse("let total: number = $expression;", "1.0").single()

        val node = assertIs<Outcome.Ok<Node>>(result).value
        assertEquals(LetDeclarationStatementNode, node.type)
    }

    @Test
    fun `parses an if block larger than the former lookahead limit`() {
        val statements = (1..100).joinToString(" ") { "println($it);" }
        val result = parse("if (flag) { $statements }", "1.1").single()

        val node = assertIs<Outcome.Ok<Node>>(result).value
        assertEquals(IfStatementNode, node.type)
        val block = (node as Node.Composite).children.elementAt(4) as Node.Composite
        assertEquals(102, block.children.size)
    }

    @Test
    fun `parses a large source as a lazy stream of top level nodes`() {
        val source = (1..10_000).joinToString("\n") { "println($it);" }
        var charactersRead = 0
        val chars = source.asSequence().onEach { charactersRead += 1 }
        val tokens = PrintScriptLexer().lex("1.0", chars).map(::tokenOrFail)

        val nodes = PrintScriptParser().parse("1.0", tokens).iterator()
        val first = nodes.next()

        assertIs<Outcome.Ok<Node>>(first)
        assertTrue(charactersRead < source.length / 100, "Parser eagerly read $charactersRead characters")
        var count = 1
        while (nodes.hasNext()) {
            val node = assertIs<Outcome.Ok<Node>>(nodes.next()).value
            count += 1
            assertEquals(count, node.span.start.line)
        }
        assertEquals(10_000, count)
        assertEquals(source.length, charactersRead)
    }

    @Test
    fun `continues after a large if and its else branch`() {
        val statements = "println(1);".repeat(1_000)
        val results = parse("if (flag) { $statements } else { println(2); } println(3);", "1.1")
        assertEquals(2, results.size)
        assertEquals(IfStatementNode, assertIs<Outcome.Ok<Node>>(results[0]).value.type)
        assertEquals(PrintlnStatementNode, assertIs<Outcome.Ok<Node>>(results[1]).value.type)
    }

    @Test
    fun `nested parenthesis errors point to the offending token`() {
        for (depth in 1..3) {
            val source = "println(" + "(".repeat(depth) + "1 + " + ")".repeat(depth) + ");"
            val index = source.indexOf(')')
            val error = assertIs<ParseError>(assertIs<Outcome.Error<Diagnostic>>(parse(source, "1.0").single()).error)
            assertEquals(
                Span(Position(1, index + 1, index), Position(1, index + 2, index + 1)),
                error.span,
            )
        }
    }

    @Test
    fun `reports an unterminated large block at end of input`() {
        val statements = (1..100).joinToString(" ") { "println($it);" }
        val result = parse("if (flag) { $statements", "1.1").single()

        val error = assertIs<Outcome.Error<Diagnostic>>(result).error
        assertEquals("Expected '}'", error.message)
    }

    private fun parse(source: String, version: String): List<Outcome<Node, Diagnostic>> {
        val tokens = PrintScriptLexer().lex(version, source.asSequence()).map(::tokenOrFail)
        return PrintScriptParser().parse(version, tokens).toList()
    }

    private fun tokenOrFail(result: Outcome<common.model.token.Token, Diagnostic>) = when (result) {
        is Outcome.Ok -> result.value
        is Outcome.Error -> error(result.error.format())
    }
}

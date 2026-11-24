import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.type.outcome.Outcome
import lexer.PrintScriptLexer
import parser.PrintScriptParser
import kotlin.test.Test
import kotlin.test.assertTrue

class ParserDiagnosticsTest {
        @Test
        fun `reports malformed statements and expressions`() {
                val cases = listOf(
                        "let", "let x", "let x:", "let x: number", "let x: number =", "let x: number = ;",
                        "let x number = 1;", "let x: = 1;", "let x: number = (1;", "let x: number = (1 + );",
                        "const", "const x", "const x:", "const x: number", "const x: number =", "const x: number = ;",
                        "x =", "x = ;", "x = (1;", "x = 1 + ;", "println", "println(", "println(1", "println(1)",
                        "println(1 + );", "println((1);", "readInput(", "let x: string = readInput(1;",
                        "let x: string = readInput();", "let x: string = readEnv(1;", "let x: string = readEnv();",
                )
                for (source in cases) {
                        val tokens = PrintScriptLexer().lex("1.1", source.asSequence()).map {
                                when (it) {
                                        is Outcome.Ok -> it.value
                                        is Outcome.Error -> error(it.error.format())
                                }
                        }
                        val results: List<Outcome<Node, Diagnostic>> = PrintScriptParser().parse("1.1", tokens).toList()
                        assertTrue(results.any { it is Outcome.Error }, "Expected a diagnostic for: $source")
                }
        }
}

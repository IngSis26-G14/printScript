package parser

import Parser
import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.model.span.Span
import common.model.token.Token
import common.type.option.Option
import common.type.outcome.Outcome
import parser.internal.buffer.TokenBuffer
import parser.internal.model.error.ConfigurationError
import parser.internal.model.error.ParseError
import parser.internal.model.scan.NodeScan
import parser.internal.scanner.NodeScanner
import parser.internal.table.GrammarTable
import parser.internal.table.GrammarTableRegistry

class PrintScriptParser : Parser {

    override fun parse(version: String, tokens: Sequence<Token>): Sequence<Outcome<Node, Diagnostic>> {
        return sequence {
            when (val table = getGrammarTable(version)) {
                is Option.Some -> {
                    for (node in parseTokens(tokens, table.value)) {
                        yield(node)
                    }
                }
                is Option.None -> {
                    yield(Outcome.Error(buildConfigurationError(version)))
                    return@sequence
                }
            }
        }
    }

    private fun getGrammarTable(ver: String): Option<GrammarTable> {
        return GrammarTableRegistry.get(ver)
    }

    private fun buildConfigurationError(version: String): Diagnostic {
        return ConfigurationError("Unsupported language version '$version'")
    }

    private fun parseTokens(
        tokens: Sequence<Token>,
        table: GrammarTable,
    ): Sequence<Outcome<Node, Diagnostic>> {
        return sequence {
            val buffer = TokenBuffer(tokens)
            val scanner = NodeScanner()

            while (buffer.hasNext()) {
                val scan = scanner.scan(buffer, table)
                when (scan) {
                    is NodeScan.Empty -> {
                        return@sequence
                    }
                    is NodeScan.Error -> {
                        yield(Outcome.Error(buildParseError(scan, buffer)))
                        return@sequence
                    }
                    is NodeScan.Ok -> {
                        yield(Outcome.Ok(scan.node))
                        buffer.advance(scan.consumed)
                    }
                }
            }
        }
    }

    private fun buildParseError(
        scan: NodeScan.Error,
        buffer: TokenBuffer,
    ): Diagnostic {
        val tokensAtError = buffer.peek(scan.consumed + 1)

        return if (tokensAtError.size > scan.consumed) {
            val errorToken = tokensAtError.elementAt(scan.consumed)
            ParseError(scan.message, scan.category, errorToken.span)
        } else {
            val insertionPoint = buffer.peek(scan.consumed).last().span.end
            ParseError(scan.message, scan.category, Span(insertionPoint, insertionPoint))
        }
    }
}

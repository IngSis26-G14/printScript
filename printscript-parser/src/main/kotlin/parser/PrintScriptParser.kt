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

            while (!buffer.isEmpty()) {
                when (val result = table.dispatchStatement(buffer)) {
                    is Outcome.Error -> {
                        yield(Outcome.Error(buildParseError(result.error, buffer)))
                        return@sequence
                    }
                    is Outcome.Ok -> {
                        yield(Outcome.Ok(result.value.node))
                        buffer.advance(result.value.consumed)
                    }
                }
            }
        }
    }

    private fun buildParseError(
        fail: parser.internal.model.grammar.GrammarFail,
        buffer: TokenBuffer,
    ): Diagnostic {
        val errorToken = buffer.tokenAt(fail.consumed)
        return if (errorToken != null) {
            ParseError(fail.message, fail.category, errorToken.span)
        } else {
            val insertionPoint = checkNotNull(buffer.tokenAt(fail.consumed - 1)).span.end
            ParseError(fail.message, fail.category, Span(insertionPoint, insertionPoint))
        }
    }
}

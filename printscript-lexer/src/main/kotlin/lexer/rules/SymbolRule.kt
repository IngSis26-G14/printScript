package lexer.rules

import common.model.diagnostic.Diagnostic
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType
import common.type.outcome.Outcome
import lexer.SourceCursor

internal class SymbolRule(
    private val symbols: Map<Char, TokenType>,
) : LexerRule {

    override fun matches(cursor: SourceCursor): Boolean = cursor.peek() in symbols

    override fun read(cursor: SourceCursor): Outcome<Token, Diagnostic> {
        val start = cursor.currentPosition()
        val symbol = checkNotNull(cursor.peek()) { "SymbolRule cannot read at EOF" }
        val type = checkNotNull(symbols[symbol]) { "SymbolRule cannot read '$symbol'" }

        cursor.advance()

        return Outcome.Ok(
            Token(
                type = type,
                lexeme = symbol.toString(),
                span = Span(start, cursor.currentPosition()),
            ),
        )
    }
}

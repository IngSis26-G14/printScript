package lexer.rules

import common.model.diagnostic.Diagnostic
import common.model.span.Position
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType
import common.type.outcome.Outcome
import lexer.SourceCursor
import lexer.categories.Lexical
import lexer.error.LexError

internal class StringRule : LexerRule {

    override fun matches(cursor: SourceCursor): Boolean = cursor.peek() == '"' || cursor.peek() == '\''

    override fun read(cursor: SourceCursor): Outcome<Token, Diagnostic> {
        check(matches(cursor)) { "StringRule must start at a quote" }

        val start = cursor.currentPosition()
        val delimiter = checkNotNull(cursor.peek())

        val lexeme = StringBuilder()
        lexeme.append(cursor.advance()) // consume la comilla de apertura

        while (true) {
            val character = cursor.peek() ?: return Outcome.Error(unterminatedString(start))

            if (character == '\n') {
                return Outcome.Error(unterminatedString(start))
            }
            lexeme.append(cursor.advance())

            if (character == delimiter) break
        }

        return Outcome.Ok(
            Token(
                type = TokenType.STRING_LITERAL,
                lexeme = lexeme.toString(),
                span = Span(start, cursor.currentPosition()),
            ),
        )
    }

    private fun unterminatedString(start: Position): LexError =
        LexError(
            message = "Unterminated string starting at line ${start.line}, column ${start.column}",
            span = Span(start, start),
            category = Lexical,
        )
}
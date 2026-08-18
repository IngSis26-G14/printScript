package lexer.rules

import common.model.diagnostic.Diagnostic
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType
import common.type.outcome.Outcome
import lexer.SourceCursor

internal class NumberRule : LexerRule {

    override fun matches(cursor: SourceCursor): Boolean = cursor.peek()?.isDigit() == true

    override fun read(cursor: SourceCursor): Outcome<Token, Diagnostic> {
        check(matches(cursor)) { "NumberRule must start at a digit" }

        val start = cursor.currentPosition()

        val lexeme = buildString {
            while (cursor.peek()?.isDigit() == true) {
                append(cursor.advance())
            }

            if (cursor.peek() == '.' && cursor.peek(1)?.isDigit() == true) {
                append(cursor.advance())

                while (cursor.peek()?.isDigit() == true) {
                    append(cursor.advance())
                }
            }
        }

        return Outcome.Ok(
            Token(
                type = TokenType.NUMBER_LITERAL,
                lexeme = lexeme,
                span = Span(start, cursor.currentPosition()),
            ),
        )
    }
}

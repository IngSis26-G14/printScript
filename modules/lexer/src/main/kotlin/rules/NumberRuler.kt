package rules

import SourceCursor
import common.source.SourceRange
import common.token.Token
import common.token.TokenType

internal class NumberRuler: LexerRule {

    override fun matches(cursor: SourceCursor): Boolean =
        cursor.peek()?.isDigit() == true

    override fun read(cursor: SourceCursor): Token {
        check(matches(cursor)) {
            "NumberRule must start at a digit"
        }

        val start = cursor.currentPosition()

        val lexeme = buildString {
            while (cursor.peek()?.isDigit() == true) {
                append(cursor.advance())
            }

            if (
                cursor.peek() == '.' &&
                cursor.peek(1)?.isDigit() == true
            ) {
                append(cursor.advance())

                while (cursor.peek()?.isDigit() == true) {
                    append(cursor.advance())
                }
            }
        }

        return Token(
            type = TokenType.NUMBER_LITERAL,
            lexeme = lexeme,
            range = SourceRange(
                start = start,
                end = cursor.currentPosition()
            ),
        )
    }
}
package rules

import SourceCursor
import common.source.SourceRange
import common.token.Token
import common.token.TokenType

internal class WordRule : LexerRule {

    private val reservedWords = mapOf(
        "let" to TokenType.LET,
        "println" to TokenType.PRINTLN,
        "number" to TokenType.TYPE_NUMBER,
        "string" to TokenType.TYPE_STRING,
    )

    override fun matches(cursor: SourceCursor): Boolean =
        cursor.peek()?.isIdentifierStart() == true

    override fun read(cursor: SourceCursor): Token {
        check(matches(cursor)) {
            "WordRule must start at a letter or underscore"
        }

        val start = cursor.currentPosition()

        val lexeme = buildString {
            while (cursor.peek()?.isIdentifierPart() == true) {
                append(cursor.advance())
            }
        }

        return Token(
            type = reservedWords[lexeme] ?: TokenType.IDENTIFIER,
            lexeme = lexeme,
            range = SourceRange(start, cursor.currentPosition()),
        )
    }

    private fun Char.isIdentifierStart(): Boolean =
        isLetter() || this == '_'

    private fun Char.isIdentifierPart(): Boolean =
        isLetterOrDigit() || this == '_'
}
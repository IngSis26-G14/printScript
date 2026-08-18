package lexer.rules

import common.model.diagnostic.Diagnostic
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType
import common.type.outcome.Outcome
import lexer.SourceCursor

internal class WordRule : LexerRule {

    private val reservedWords = mapOf(
        "let" to TokenType.LET,
        "println" to TokenType.PRINTLN,
        "number" to TokenType.TYPE_NUMBER,
        "string" to TokenType.TYPE_STRING,
    )

    override fun matches(cursor: SourceCursor): Boolean = cursor.peek()?.isIdentifierStart() == true

    override fun read(cursor: SourceCursor): Outcome<Token, Diagnostic> {
        check(matches(cursor)) { "WordRule must start at a letter or underscore" }

        val start = cursor.currentPosition()
        val lexeme = buildString {
            while (cursor.peek()?.isIdentifierPart() == true) {
                append(cursor.advance())
            }
        }

        return Outcome.Ok(
            Token(
                type = reservedWords[lexeme] ?: TokenType.IDENTIFIER,
                lexeme = lexeme,
                span = Span(start, cursor.currentPosition()),
            ),
        )
    }

    private fun Char.isIdentifierStart(): Boolean = isLetter() || this == '_'
    private fun Char.isIdentifierPart(): Boolean = isLetterOrDigit() || this == '_'
}

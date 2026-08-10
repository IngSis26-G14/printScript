package rules

import SourceCursor
import common.source.SourceRange
import common.token.Token
import common.token.TokenType

internal class WordRule: LexerRule {

    private val reservedWords = mapOf(
        "let" to TokenType.LET,
        "println" to TokenType.PRINTLN,
        "number" to TokenType.TYPE_NUMBER,
        "string" to TokenType.TYPE_STRING,
    )


    override fun matches(cursor: SourceCursor): Boolean {
        val character = cursor.peek()
        return character?.isLetter() == true || character == '_'
    }

    override fun read(cursor: SourceCursor): Token {
       val start = cursor.currentPosition()

        val lexeme = buildString {
            while ( cursor.peek()?.isIdentifierCharacter() == true){
                append(cursor.advance())
            }
        }

        return Token(
            type = reservedWords[lexeme] ?: TokenType.IDENTIFIER,
            lexeme = lexeme,
            range = SourceRange(start, cursor.currentPosition()),
        )
    }

    private fun Char.isIdentifierCharacter(): Boolean =
        isLetterOrDigit() || this == '_'
}
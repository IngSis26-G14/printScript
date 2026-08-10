package rules

import LexicalException
import SourceCursor
import common.source.SourcePosition
import common.source.SourceRange
import common.token.Token
import common.token.TokenType

internal class StringRule: LexerRule {


    override fun matches(cursor: SourceCursor): Boolean =
        cursor.peek() == '"'

    override fun read(cursor: SourceCursor): Token {
        check(matches(cursor)){
            "StringRule must start at a double quote"
        }

        val start = cursor.currentPosition()

        val lexeme = buildString {
            append(cursor.advance()) // consume the opening double quote

            while(true){
                val character = cursor.peek()
                    ?: throw unterminatedString(start)

                if(character == '\n') {
                    throw unterminatedString(start)
                }

                append(cursor.advance())

                if (character == '"'){
                    break
                }
            }
        }
        return Token(
            type = TokenType.STRING_LITERAL,
            lexeme = lexeme,
            range = SourceRange(
                start = start,
                end = cursor.currentPosition(),
            ),
        )
    }

    private fun unterminatedString(start: SourcePosition): LexicalException =
        LexicalException(
            message = "Unterminate string at line ${start.line}," + "column ${start.column}",
            position = start,
    )
}
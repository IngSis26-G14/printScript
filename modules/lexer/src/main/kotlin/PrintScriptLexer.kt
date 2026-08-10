import common.token.TokenType
import common.source.SourceRange
import common.token.Token
import java.io.Reader

class PrintScriptLexer: Lexer {


    private val singleCharacterTokens = mapOf(
        '(' to TokenType.LEFT_PARENTHESIS,
        ')' to TokenType.RIGHT_PARENTHESIS,
        '=' to TokenType.ASSIGNMENT_OPERATOR,
        '-' to TokenType.SUBTRACTION_OPERATOR,
        '+' to TokenType.ADDITION_OPERATOR,
        '/' to TokenType.DIVISION_OPERATOR,
        '*' to TokenType.MULTIPLICATION_OPERATOR,
        ';' to TokenType.SEMICOLON,
        ':' to TokenType.COLON,
    )

    override fun tokenize(source: Reader): Sequence<Token> {
        val cursor = SourceCursor(source.readText())
        val tokens = mutableListOf<Token>()

        while(!cursor.isAtEnd()){
            val character = cursor.peek()!!

            when{
                character.isWhitespace() -> {
                    cursor.advance()
                }

                singleCharacterTokens.containsKey(character)-> {
                    tokens.add(readSingleCharacterToken(cursor))
                }

                else -> {
                    val position = cursor.currentPosition()

                    throw LexicalException(
                        message = "Unexpected character: '$character'" + "at line ${position.line}, column ${position.column}",
                        position = position
                    )
                }
            }
        }
        tokens.add(createEndOfFileToken(cursor))
        return tokens.asSequence()
    }

    private fun readSingleCharacterToken(
        cursor: SourceCursor,
    ): Token {
        val start = cursor.currentPosition()
        val character = cursor.advance()
        val end = cursor.currentPosition()

        val type =
            requireNotNull(singleCharacterTokens[character]) {
                "Character '$character' is not a registered token"
            }

        return Token(
            type = type,
            lexeme = character.toString(),
            range = SourceRange(
                start = start,
                end = end,
            ),
        )
    }

    private fun createEndOfFileToken(
        cursor: SourceCursor,
    ): Token {
        val position = cursor.currentPosition()

        return Token(
            type = TokenType.EOF,
            lexeme = "",
            range = SourceRange(
                start = position,
                end = position,
            ),
        )
    }
}
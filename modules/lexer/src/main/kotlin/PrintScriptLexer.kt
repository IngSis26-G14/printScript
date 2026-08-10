import common.source.SourceRange
import common.token.Token
import common.token.TokenType
import rules.LexerRule
import rules.NumberRule
import rules.StringRule
import rules.SymbolRule
import rules.WordRule
import java.io.Reader

class PrintScriptLexer internal constructor(
    private val rules: List<LexerRule>,
    ) : Lexer {

    constructor() : this(
        listOf(
            StringRule(),
            NumberRule(),
            WordRule(),
            SymbolRule(),
        )
    )


    override fun tokenize(source: Reader): Sequence<Token> =
        sequence {
            val cursor = SourceCursor(source)

            while (!cursor.isAtEnd()) {
                skipWhiteSpace(cursor)

                if (cursor.isAtEnd()) {
                    break
                }

                val rule = rules.firstOrNull() { it.matches(cursor) }
                    ?: throw unexpectedCharacter(cursor)

                val previousOffset = cursor.offset
                val token = rule.read(cursor)

                check(cursor.offset > previousOffset) {
                    "${rule::class.simpleName} returned a token " + "without consuming input"
                }

                yield(token)
            }
            yield(createEndOfFileToken(cursor))
        }.constrainOnce()


    private fun skipWhiteSpace(cursor: SourceCursor) {
        while (cursor.peek()?.isWhitespace() == true) {
            cursor.advance()
        }
    }

    private fun unexpectedCharacter(cursor: SourceCursor,
    ): LexicalException {
        val position = cursor.currentPosition()
        val character = cursor.peek()

        return LexicalException(
            message =
                "Unexpected character '$character' at " +
                    "line ${position.line}, column ${position.column}",
            position = position,
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




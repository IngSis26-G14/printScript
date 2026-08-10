import common.token.TokenType
import org.junit.jupiter.api.Assertions.*

import kotlin.test.Test
import kotlin.test.assertEquals

class PrintScriptLexerTest {

    private val lexer = PrintScriptLexer()

    @Test
    fun `tokenizes operators and punctuation`() {
        val source =":;=+-*/()".reader()

        val tokens = lexer.tokenize(source).toList()
        assertEquals(
            listOf(
                TokenType.COLON,
                TokenType.SEMICOLON,
                TokenType.ASSIGNMENT_OPERATOR,
                TokenType.ADDITION_OPERATOR,
                TokenType.SUBTRACTION_OPERATOR,
                TokenType.MULTIPLICATION_OPERATOR,
                TokenType.DIVISION_OPERATOR,
                TokenType.LEFT_PARENTHESIS,
                TokenType.RIGHT_PARENTHESIS,
                TokenType.EOF,
            ),
            tokens.map { it.type },
        )
    }

    @Test
    fun `ignores whitespace`() {
        val source = "  + \n -  ".reader()

        val tokens = lexer.tokenize(source).toList()

        assertEquals(
            listOf(
                TokenType.ADDITION_OPERATOR,
                TokenType.SUBTRACTION_OPERATOR,
                TokenType.EOF,
            ),
            tokens.map { it.type },
        )
    }

    @Test
    fun `stores the original lexeme`() {
        val source = "+;".reader()

        val tokens = lexer.tokenize(source).toList()

        assertEquals("+", tokens[0].lexeme)
        assertEquals(";", tokens[1].lexeme)
    }
}
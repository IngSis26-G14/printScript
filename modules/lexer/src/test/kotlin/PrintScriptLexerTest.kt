import common.source.SourcePosition
import common.token.TokenType
import lexer.LexicalException
import lexer.PrintScriptLexer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun `tokenizes a string literal`() {
        val source = "'Hello, World!'".reader()

        val tokens = lexer.tokenize(source).toList()

        assertEquals(
            listOf(
                TokenType.STRING_LITERAL,
                TokenType.EOF,
            ),
            tokens.map { it.type },
        )
    }

    @Test
    fun `tokenizes a number literal`() {
        val source = "12.5".reader()

        val tokens = lexer.tokenize(source).toList()

        assertEquals(
            listOf(
                TokenType.NUMBER_LITERAL,
                TokenType.EOF,
            ),
            tokens.map { it.type },
        )
    }


    @Test
    fun `tokenizes a variable declaration`() {
        val source = "let name: string = 'Joe';".reader()

        val tokens = lexer.tokenize(source).toList()

        assertEquals(
            listOf(
                TokenType.LET,
                TokenType.IDENTIFIER,
                TokenType.COLON,
                TokenType.TYPE_STRING,
                TokenType.ASSIGNMENT_OPERATOR,
                TokenType.STRING_LITERAL,
                TokenType.SEMICOLON,
                TokenType.EOF,
            ),
            tokens.map { it.type },
        )
    }

    @Test
    fun `tokenizes a complete PrintScript program`() {
        val source = """
        let name: string = 'Joe';
        let result: number = 12.5 + 4;
        println(name + " result");
    """.trimIndent().reader()

        val types = lexer.tokenize(source)
            .map { it.type }
            .toList()

        assertEquals(
            listOf(
                TokenType.LET,
                TokenType.IDENTIFIER,
                TokenType.COLON,
                TokenType.TYPE_STRING,
                TokenType.ASSIGNMENT_OPERATOR,
                TokenType.STRING_LITERAL,
                TokenType.SEMICOLON,

                TokenType.LET,
                TokenType.IDENTIFIER,
                TokenType.COLON,
                TokenType.TYPE_NUMBER,
                TokenType.ASSIGNMENT_OPERATOR,
                TokenType.NUMBER_LITERAL,
                TokenType.ADDITION_OPERATOR,
                TokenType.NUMBER_LITERAL,
                TokenType.SEMICOLON,

                TokenType.PRINTLN,
                TokenType.LEFT_PARENTHESIS,
                TokenType.IDENTIFIER,
                TokenType.ADDITION_OPERATOR,
                TokenType.STRING_LITERAL,
                TokenType.RIGHT_PARENTHESIS,
                TokenType.SEMICOLON,

                TokenType.EOF,
            ),
            types,
        )
    }


    @Test
    fun `throws lexical exception for unexpected character`() {
        val exception = assertFailsWith<LexicalException> {
            lexer.tokenize("let @".reader()).toList()
        }

        assertEquals(
            SourcePosition(
                line = 1,
                column = 5,
                offset = 4,
            ),
            exception.position,
        )

        assertEquals(
            "Unexpected character '@' at line 1, column 5",
            exception.message,
        )
    }

    @Test
    fun `throws lexical exception for unterminated string`() {
        val exception = assertFailsWith<LexicalException> {
            lexer.tokenize("\"Hello".reader()).toList()
        }

        assertEquals(
            SourcePosition(
                line = 1,
                column = 1,
                offset = 0,
            ),
            exception.position,
        )

        assertEquals(
            "Unterminated string at line 1, column 1",
            exception.message,
        )
    }
}
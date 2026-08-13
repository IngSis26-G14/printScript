package lexer

import common.model.diagnostic.Diagnostic
import common.model.span.Position
import common.model.token.Token
import common.model.token.TokenType
import common.type.outcome.Outcome
import lexer.error.LexError

import kotlin.test.Test
import kotlin.test.assertEquals

class PrintScriptLexerTest {

    private val lexer = PrintScriptLexer()

    private fun lex(source: String): List<Outcome<Token, Diagnostic>> =
        lexer.lex("1.0", source.asSequence()).toList()

    private fun tokensOf(outcomes: List<Outcome<Token, Diagnostic>>): List<Token> =
        outcomes.map {
            require(it is Outcome.Ok<Token>) { "Expected Ok, got $it" }
            it.value
        }

    private fun lastError(outcomes: List<Outcome<Token, Diagnostic>>): LexError {
        val last = outcomes.last()
        require(last is Outcome.Error<Diagnostic>) { "Expected the last item to be an Error, got $last" }
        val error = last.error
        require(error is LexError) { "Expected a LexError, got $error" }
        return error
    }

    @Test
    fun `tokenizes operators and punctuation`() {
        val tokens = tokensOf(lex(":;=+-*/()"))

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
            ),
            tokens.map { it.type },
        )
    }

    @Test
    fun `ignores whitespace`() {
        val tokens = tokensOf(lex("  + \n -  "))

        assertEquals(
            listOf(TokenType.ADDITION_OPERATOR, TokenType.SUBTRACTION_OPERATOR),
            tokens.map { it.type },
        )
    }

    @Test
    fun `stores the original lexeme`() {
        val tokens = tokensOf(lex("+;"))

        assertEquals("+", tokens[0].lexeme)
        assertEquals(";", tokens[1].lexeme)
    }

    @Test
    fun `tokenizes a string literal`() {
        val tokens = tokensOf(lex("'Hello, World!'"))

        assertEquals(listOf(TokenType.STRING_LITERAL), tokens.map { it.type })
    }

    @Test
    fun `tokenizes a number literal`() {
        val tokens = tokensOf(lex("12.5"))

        assertEquals(listOf(TokenType.NUMBER_LITERAL), tokens.map { it.type })
    }

    @Test
    fun `tokenizes a variable declaration`() {
        val tokens = tokensOf(lex("let name: string = 'Joe';"))

        assertEquals(
            listOf(
                TokenType.LET,
                TokenType.IDENTIFIER,
                TokenType.COLON,
                TokenType.TYPE_STRING,
                TokenType.ASSIGNMENT_OPERATOR,
                TokenType.STRING_LITERAL,
                TokenType.SEMICOLON,
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
        """.trimIndent()

        val tokens = tokensOf(lex(source))

        assertEquals(
            listOf(
                TokenType.LET, TokenType.IDENTIFIER, TokenType.COLON, TokenType.TYPE_STRING,
                TokenType.ASSIGNMENT_OPERATOR, TokenType.STRING_LITERAL, TokenType.SEMICOLON,

                TokenType.LET, TokenType.IDENTIFIER, TokenType.COLON, TokenType.TYPE_NUMBER,
                TokenType.ASSIGNMENT_OPERATOR, TokenType.NUMBER_LITERAL, TokenType.ADDITION_OPERATOR,
                TokenType.NUMBER_LITERAL, TokenType.SEMICOLON,

                TokenType.PRINTLN, TokenType.LEFT_PARENTHESIS, TokenType.IDENTIFIER,
                TokenType.ADDITION_OPERATOR, TokenType.STRING_LITERAL, TokenType.RIGHT_PARENTHESIS,
                TokenType.SEMICOLON,
            ),
            tokens.map { it.type },
        )
    }

    @Test
    fun `reports a diagnostic for an unexpected character`() {
        val error = lastError(lex("let @"))

        assertEquals(Position(line = 1, column = 5, index = 4), error.span.start)
        assertEquals("Unexpected character '@'", error.message)
    }

    @Test
    fun `reports a diagnostic for an unterminated string`() {
        val error = lastError(lex("\"Hello"))

        assertEquals(Position(line = 1, column = 1, index = 0), error.span.start)
        assertEquals("Unterminated string starting at line 1, column 1", error.message)
    }
}
package util

import common.model.span.Position
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType

internal object PrintScriptV10TestSnippets {

    init {
        TestSnippetRegistry.register(
            "empty-println-statement",
            listOf(
                Token(TokenType.PRINTLN, "println", Span(Position(1, 1, 0), Position(1, 8, 7))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 8, 7), Position(1, 9, 8))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 9, 8), Position(1, 10, 9))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 10, 9), Position(1, 11, 10))),
            ),
        )

        TestSnippetRegistry.register(
            "expression-as-statement",
            listOf(
                Token(TokenType.READ_INPUT, "readInput", Span(Position(1, 1, 0), Position(1, 9, 8))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 9, 8), Position(1, 10, 9))),
                Token(TokenType.STRING_LITERAL, "\"Enter a number: \"", Span(Position(1, 10, 9), Position(1, 29, 28))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 29, 28), Position(1, 30, 29))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 30, 29), Position(1, 31, 30))),
            ),
        )

        TestSnippetRegistry.register(
            "identifier-and-number-binary-expression",
            listOf(
                Token(TokenType.LET, "let", Span(Position(1, 1, 0), Position(1, 4, 3))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 5, 4), Position(1, 6, 5))),
                Token(TokenType.COLON, ":", Span(Position(1, 6, 5), Position(1, 7, 6))),
                Token(TokenType.TYPE_NUMBER, "number", Span(Position(1, 8, 7), Position(1, 14, 13))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(1, 15, 14), Position(1, 16, 15))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 17, 16), Position(1, 18, 17))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 18, 17), Position(1, 19, 18))),
                Token(TokenType.MULTIPLICATION_OPERATOR, "*", Span(Position(1, 19, 18), Position(1, 20, 19))),
                Token(TokenType.NUMBER_LITERAL, "5", Span(Position(1, 21, 20), Position(1, 22, 21))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 22, 21), Position(1, 23, 22))),
                Token(TokenType.SUBTRACTION_OPERATOR, "-", Span(Position(1, 24, 23), Position(1, 25, 24))),
                Token(TokenType.IDENTIFIER, "y", Span(Position(1, 26, 25), Position(1, 27, 26))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 28, 27), Position(1, 29, 28))),
            ),
        )

        TestSnippetRegistry.register(
            "identifier-and-string-binary-expression",
            listOf(
                Token(TokenType.LET, "let", Span(Position(1, 1, 0), Position(1, 4, 3))),
                Token(TokenType.IDENTIFIER, "messageEnd", Span(Position(1, 5, 4), Position(1, 15, 14))),
                Token(TokenType.COLON, ":", Span(Position(1, 15, 14), Position(1, 16, 15))),
                Token(TokenType.TYPE_STRING, "string", Span(Position(1, 17, 16), Position(1, 23, 22))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(1, 24, 23), Position(1, 25, 24))),
                Token(TokenType.STRING_LITERAL, "\" World!\"", Span(Position(1, 26, 25), Position(1, 35, 34))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 35, 34), Position(1, 36, 35))),

                Token(TokenType.LET, "let", Span(Position(2, 1, 36), Position(2, 4, 39))),
                Token(TokenType.IDENTIFIER, "message", Span(Position(2, 5, 40), Position(2, 12, 47))),
                Token(TokenType.COLON, ":", Span(Position(2, 12, 47), Position(2, 13, 48))),
                Token(TokenType.TYPE_STRING, "string", Span(Position(2, 14, 49), Position(2, 20, 55))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(2, 21, 56), Position(2, 22, 57))),
                Token(TokenType.STRING_LITERAL, "\"Hello,\"", Span(Position(2, 23, 58), Position(2, 31, 66))),
                Token(TokenType.ADDITION_OPERATOR, "+", Span(Position(2, 32, 67), Position(2, 33, 68))),
                Token(TokenType.IDENTIFIER, "messageEnd", Span(Position(2, 34, 69), Position(2, 45, 80))),
                Token(TokenType.SEMICOLON, ";", Span(Position(2, 46, 81), Position(2, 47, 82))),
            ),
        )

        TestSnippetRegistry.register(
            "let-assignation-statement",
            listOf(
                Token(TokenType.LET, "let", Span(Position(1, 1, 0), Position(1, 4, 3))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 5, 4), Position(1, 6, 5))),
                Token(TokenType.COLON, ":", Span(Position(1, 6, 5), Position(1, 7, 6))),
                Token(TokenType.TYPE_NUMBER, "number", Span(Position(1, 8, 7), Position(1, 14, 13))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 14, 13), Position(1, 15, 14))),

                Token(TokenType.IDENTIFIER, "x", Span(Position(2, 1, 15), Position(2, 2, 16))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(2, 3, 17), Position(2, 4, 18))),
                Token(TokenType.NUMBER_LITERAL, "5", Span(Position(2, 5, 19), Position(2, 6, 20))),
                Token(TokenType.SEMICOLON, ";", Span(Position(2, 6, 20), Position(2, 7, 21))),

                Token(TokenType.LET, "let", Span(Position(3, 1, 22), Position(3, 4, 25))),
                Token(TokenType.IDENTIFIER, "y", Span(Position(3, 5, 26), Position(3, 6, 27))),
                Token(TokenType.COLON, ":", Span(Position(3, 6, 27), Position(3, 7, 28))),
                Token(TokenType.TYPE_STRING, "string", Span(Position(3, 8, 29), Position(3, 14, 35))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(3, 15, 36), Position(3, 16, 37))),
                Token(TokenType.STRING_LITERAL, "\"Hello\"", Span(Position(3, 17, 38), Position(3, 24, 45))),
                Token(TokenType.SEMICOLON, ";", Span(Position(3, 24, 45), Position(3, 25, 46))),

                Token(TokenType.IDENTIFIER, "y", Span(Position(4, 1, 47), Position(4, 2, 48))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(4, 3, 49), Position(4, 4, 50))),
                Token(TokenType.STRING_LITERAL, "\"Goodbye\"", Span(Position(4, 5, 51), Position(4, 14, 60))),
                Token(TokenType.SEMICOLON, ";", Span(Position(4, 14, 60), Position(4, 15, 61))),
            ),
        )

        TestSnippetRegistry.register(
            "let-declaration-statement",
            listOf(
                Token(TokenType.LET, "let", Span(Position(1, 1, 0), Position(1, 4, 3))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 5, 4), Position(1, 6, 5))),
                Token(TokenType.COLON, ":", Span(Position(1, 6, 5), Position(1, 7, 6))),
                Token(TokenType.TYPE_NUMBER, "number", Span(Position(1, 8, 7), Position(1, 14, 13))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 14, 13), Position(1, 15, 14))),

                Token(TokenType.LET, "let", Span(Position(2, 1, 15), Position(2, 4, 18))),
                Token(TokenType.IDENTIFIER, "y", Span(Position(2, 5, 19), Position(2, 6, 20))),
                Token(TokenType.COLON, ":", Span(Position(2, 6, 20), Position(2, 7, 21))),
            ),
        )

        TestSnippetRegistry.register(
            "missing-end-of-statement",
            listOf(
                Token(TokenType.PRINTLN, "println", Span(Position(1, 1, 0), Position(1, 7, 6))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 7, 6), Position(1, 8, 7))),
                Token(TokenType.STRING_LITERAL, "\"Hello, World!\"", Span(Position(1, 8, 7), Position(1, 26, 25))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 26, 25), Position(1, 27, 26))),
            ),
        )

        TestSnippetRegistry.register(
            "println-statement",
            listOf(
                Token(TokenType.PRINTLN, "println", Span(Position(1, 1, 0), Position(1, 7, 6))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 7, 6), Position(1, 8, 7))),
                Token(TokenType.STRING_LITERAL, "\"Hello, World!\"", Span(Position(1, 8, 7), Position(1, 26, 25))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 26, 25), Position(1, 27, 26))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 27, 26), Position(1, 28, 27))),
            ),
        )

        TestSnippetRegistry.register(
            "println-with-binary-operation",
            listOf(
                Token(TokenType.PRINTLN, "println", Span(Position(1, 1, 0), Position(1, 7, 6))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 7, 6), Position(1, 8, 7))),
                Token(TokenType.NUMBER_LITERAL, "5", Span(Position(1, 8, 7), Position(1, 9, 8))),
                Token(TokenType.MULTIPLICATION_OPERATOR, "*", Span(Position(1, 9, 8), Position(1, 10, 9))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 10, 9), Position(1, 11, 10))),
                Token(TokenType.NUMBER_LITERAL, "8", Span(Position(1, 11, 10), Position(1, 12, 11))),
                Token(TokenType.SUBTRACTION_OPERATOR, "-", Span(Position(1, 12, 11), Position(1, 13, 12))),
                Token(TokenType.NUMBER_LITERAL, "3", Span(Position(1, 13, 12), Position(1, 14, 13))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 14, 13), Position(1, 15, 14))),
                Token(TokenType.ADDITION_OPERATOR, "+", Span(Position(1, 15, 14), Position(1, 16, 15))),
                Token(TokenType.NUMBER_LITERAL, "4", Span(Position(1, 16, 15), Position(1, 17, 16))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 17, 16), Position(1, 18, 17))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 18, 17), Position(1, 19, 18))),
            ),
        )

        TestSnippetRegistry.register(
            "string-and-number-binary-expression",
            listOf(
                Token(TokenType.LET, "let", Span(Position(1, 1, 0), Position(1, 4, 3))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 5, 4), Position(1, 6, 5))),
                Token(TokenType.COLON, ":", Span(Position(1, 6, 5), Position(1, 7, 6))),
                Token(TokenType.TYPE_STRING, "string", Span(Position(1, 8, 7), Position(1, 14, 13))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(1, 15, 14), Position(1, 16, 15))),
                Token(TokenType.STRING_LITERAL, "\"Hello \"", Span(Position(1, 17, 16), Position(1, 25, 24))),
                Token(TokenType.ADDITION_OPERATOR, "+", Span(Position(1, 26, 25), Position(1, 27, 26))),
                Token(TokenType.NUMBER_LITERAL, "123", Span(Position(1, 28, 27), Position(1, 31, 30))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 31, 30), Position(1, 32, 31))),
            ),
        )
    }
}

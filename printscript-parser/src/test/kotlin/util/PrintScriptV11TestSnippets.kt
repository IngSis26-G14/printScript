package util

import common.model.span.Position
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType

object PrintScriptV11TestSnippets {

    init {
        TestSnippetRegistry.register(
            "const-declaration-statement",
            listOf(
                Token(TokenType.CONST, "const", Span(Position(1, 1, 0), Position(1, 5, 4))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 6, 5), Position(1, 7, 6))),
                Token(TokenType.COLON, ":", Span(Position(1, 7, 6), Position(1, 8, 7))),
                Token(TokenType.TYPE_BOOLEAN, "boolean", Span(Position(1, 9, 8), Position(1, 16, 15))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 16, 15), Position(1, 17, 16))),
                Token(TokenType.CONST, "const", Span(Position(2, 1, 17), Position(2, 5, 21))),
                Token(TokenType.IDENTIFIER, "y", Span(Position(2, 6, 22), Position(2, 7, 23))),
                Token(TokenType.COLON, ":", Span(Position(2, 7, 23), Position(2, 8, 24))),
            ),
        )

        TestSnippetRegistry.register(
            "empty-readEnv-expression",
            listOf(
                Token(TokenType.LET, "let", Span(Position(1, 1, 0), Position(1, 4, 3))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 5, 4), Position(1, 6, 5))),
                Token(TokenType.COLON, ":", Span(Position(1, 6, 5), Position(1, 7, 6))),
                Token(TokenType.TYPE_STRING, "string", Span(Position(1, 8, 7), Position(1, 14, 13))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(1, 15, 14), Position(1, 16, 15))),
                Token(TokenType.READ_ENV, "readEnv", Span(Position(1, 17, 16), Position(1, 24, 23))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 24, 23), Position(1, 25, 24))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 25, 24), Position(1, 26, 25))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 26, 25), Position(1, 27, 26))),
            ),
        )

        TestSnippetRegistry.register(
            "empty-readInput-expression",
            listOf(
                Token(TokenType.LET, "let", Span(Position(1, 1, 0), Position(1, 4, 3))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 5, 4), Position(1, 6, 5))),
                Token(TokenType.COLON, ":", Span(Position(1, 6, 5), Position(1, 7, 6))),
                Token(TokenType.TYPE_NUMBER, "number", Span(Position(1, 8, 7), Position(1, 14, 13))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(1, 15, 14), Position(1, 16, 15))),
                Token(TokenType.READ_INPUT, "readInput", Span(Position(1, 17, 16), Position(1, 26, 25))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 26, 25), Position(1, 27, 26))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 27, 26), Position(1, 28, 27))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 28, 27), Position(1, 29, 28))),
            ),
        )

        TestSnippetRegistry.register(
            "if-else-statement",
            listOf(
                Token(TokenType.LET, "let", Span(Position(1, 1, 0), Position(1, 4, 3))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 5, 4), Position(1, 6, 5))),
                Token(TokenType.COLON, ":", Span(Position(1, 6, 5), Position(1, 7, 6))),
                Token(TokenType.TYPE_BOOLEAN, "boolean", Span(Position(1, 8, 7), Position(1, 15, 14))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(1, 16, 15), Position(1, 17, 16))),
                Token(TokenType.BOOLEAN_LITERAL, "true", Span(Position(1, 18, 17), Position(1, 22, 21))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 22, 21), Position(1, 23, 22))),

                Token(TokenType.IF, "if", Span(Position(2, 1, 23), Position(2, 3, 25))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(2, 3, 25), Position(2, 4, 26))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(2, 4, 26), Position(2, 5, 27))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(2, 5, 27), Position(2, 6, 28))),
                Token(TokenType.LEFT_BRACE, "{", Span(Position(2, 7, 29), Position(2, 8, 30))),

                Token(TokenType.PRINTLN, "println", Span(Position(3, 1, 31), Position(3, 8, 38))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(3, 8, 38), Position(3, 9, 39))),
                Token(TokenType.STRING_LITERAL, "\"x is true\"", Span(Position(3, 9, 39), Position(3, 21, 51))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(3, 21, 51), Position(3, 22, 52))),
                Token(TokenType.SEMICOLON, ";", Span(Position(3, 22, 52), Position(3, 23, 53))),

                Token(TokenType.RIGHT_BRACE, "}", Span(Position(4, 1, 54), Position(4, 2, 55))),
                Token(TokenType.ELSE, "else", Span(Position(4, 3, 56), Position(4, 7, 60))),
                Token(TokenType.LEFT_BRACE, "{", Span(Position(4, 8, 61), Position(4, 9, 62))),

                Token(TokenType.PRINTLN, "println", Span(Position(5, 1, 63), Position(5, 8, 70))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(5, 8, 70), Position(5, 9, 71))),
                Token(TokenType.STRING_LITERAL, "\"x is false\"", Span(Position(5, 9, 71), Position(5, 21, 83))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(5, 21, 83), Position(5, 22, 84))),
                Token(TokenType.SEMICOLON, ";", Span(Position(5, 22, 84), Position(5, 23, 85))),

                Token(TokenType.RIGHT_BRACE, "}", Span(Position(6, 1, 86), Position(6, 2, 87))),
            ),
        )

        TestSnippetRegistry.register(
            "if-statement",
            listOf(
                Token(TokenType.IF, "if", Span(Position(1, 1, 0), Position(1, 3, 2))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 3, 2), Position(1, 4, 3))),
                Token(TokenType.BOOLEAN_LITERAL, "true", Span(Position(1, 4, 3), Position(1, 8, 7))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 8, 7), Position(1, 9, 8))),
                Token(TokenType.LEFT_BRACE, "{", Span(Position(1, 10, 9), Position(1, 11, 10))),

                Token(TokenType.PRINTLN, "println", Span(Position(2, 1, 11), Position(2, 8, 18))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(2, 8, 18), Position(2, 9, 19))),
                Token(TokenType.STRING_LITERAL, "\"Hello, World!\"", Span(Position(2, 9, 19), Position(2, 26, 36))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(2, 26, 36), Position(2, 27, 37))),
                Token(TokenType.SEMICOLON, ";", Span(Position(2, 27, 37), Position(2, 28, 38))),

                Token(TokenType.RIGHT_BRACE, "}", Span(Position(3, 1, 39), Position(3, 2, 40))),
            ),
        )

        TestSnippetRegistry.register(
            "nested-if-else-statement",
            listOf(
                Token(TokenType.CONST, "const", Span(Position(1, 1, 0), Position(1, 6, 5))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 7, 6), Position(1, 8, 7))),
                Token(TokenType.COLON, ":", Span(Position(1, 8, 7), Position(1, 9, 8))),
                Token(TokenType.TYPE_BOOLEAN, "boolean", Span(Position(1, 10, 9), Position(1, 17, 16))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(1, 18, 17), Position(1, 19, 18))),
                Token(TokenType.BOOLEAN_LITERAL, "true", Span(Position(1, 20, 19), Position(1, 24, 23))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 24, 23), Position(1, 25, 24))),

                Token(TokenType.CONST, "const", Span(Position(2, 1, 25), Position(2, 6, 30))),
                Token(TokenType.IDENTIFIER, "y", Span(Position(2, 7, 31), Position(2, 8, 32))),
                Token(TokenType.COLON, ":", Span(Position(2, 8, 32), Position(2, 9, 33))),
                Token(TokenType.TYPE_BOOLEAN, "boolean", Span(Position(2, 10, 34), Position(2, 17, 41))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(2, 18, 42), Position(2, 19, 43))),
                Token(TokenType.BOOLEAN_LITERAL, "true", Span(Position(2, 20, 44), Position(2, 24, 48))),
                Token(TokenType.SEMICOLON, ";", Span(Position(2, 24, 48), Position(2, 25, 49))),

                Token(TokenType.IF, "if", Span(Position(3, 1, 50), Position(3, 3, 52))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(3, 3, 52), Position(3, 4, 53))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(3, 4, 53), Position(3, 5, 54))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(3, 5, 54), Position(3, 6, 55))),
                Token(TokenType.LEFT_BRACE, "{", Span(Position(3, 7, 56), Position(3, 8, 57))),

                Token(TokenType.IF, "if", Span(Position(4, 2, 58), Position(4, 4, 60))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(4, 4, 60), Position(4, 5, 61))),
                Token(TokenType.IDENTIFIER, "y", Span(Position(4, 5, 61), Position(4, 6, 62))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(4, 6, 62), Position(4, 7, 63))),
                Token(TokenType.LEFT_BRACE, "{", Span(Position(4, 8, 64), Position(4, 9, 65))),

                Token(TokenType.PRINTLN, "println", Span(Position(5, 3, 66), Position(5, 9, 72))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(5, 9, 72), Position(5, 10, 73))),
                Token(TokenType.STRING_LITERAL, "\"x and y is true\"", Span(Position(5, 10, 73), Position(5, 28, 91))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(5, 28, 91), Position(5, 29, 92))),
                Token(TokenType.SEMICOLON, ";", Span(Position(5, 29, 92), Position(5, 30, 93))),

                Token(TokenType.RIGHT_BRACE, "}", Span(Position(6, 2, 94), Position(6, 3, 95))),
                Token(TokenType.ELSE, "else", Span(Position(6, 4, 96), Position(6, 8, 100))),
                Token(TokenType.LEFT_BRACE, "{", Span(Position(6, 9, 101), Position(6, 10, 102))),

                Token(TokenType.PRINTLN, "println", Span(Position(7, 3, 103), Position(7, 9, 109))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(7, 9, 109), Position(7, 10, 110))),
                Token(TokenType.STRING_LITERAL, "\"x is true, y is false\"", Span(Position(7, 10, 110), Position(7, 31, 131))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(7, 31, 131), Position(7, 32, 132))),
                Token(TokenType.SEMICOLON, ";", Span(Position(7, 32, 132), Position(7, 33, 133))),

                Token(TokenType.RIGHT_BRACE, "}", Span(Position(8, 2, 134), Position(8, 3, 135))),

                Token(TokenType.RIGHT_BRACE, "}", Span(Position(9, 2, 136), Position(9, 3, 137))),
                Token(TokenType.ELSE, "else", Span(Position(9, 4, 138), Position(9, 8, 142))),
                Token(TokenType.LEFT_BRACE, "{", Span(Position(9, 9, 143), Position(9, 10, 144))),

                Token(TokenType.PRINTLN, "println", Span(Position(10, 3, 145), Position(10, 9, 151))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(10, 9, 151), Position(10, 10, 152))),
                Token(TokenType.STRING_LITERAL, "\"x is false\"", Span(Position(10, 10, 152), Position(10, 24, 166))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(10, 24, 166), Position(10, 25, 167))),
                Token(TokenType.SEMICOLON, ";", Span(Position(10, 25, 167), Position(10, 26, 168))),

                Token(TokenType.RIGHT_BRACE, "}", Span(Position(11, 1, 168), Position(11, 2, 169))),
            ),
        )

        TestSnippetRegistry.register(
            "readEnv-expression",
            listOf(
                Token(TokenType.LET, "let", Span(Position(1, 1, 0), Position(1, 4, 3))),
                Token(TokenType.IDENTIFIER, "x", Span(Position(1, 5, 4), Position(1, 6, 5))),
                Token(TokenType.COLON, ":", Span(Position(1, 6, 5), Position(1, 7, 6))),
                Token(TokenType.TYPE_NUMBER, "number", Span(Position(1, 8, 7), Position(1, 14, 13))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(1, 15, 14), Position(1, 16, 15))),
                Token(TokenType.READ_ENV, "readEnv", Span(Position(1, 17, 16), Position(1, 24, 23))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 24, 23), Position(1, 25, 24))),
                Token(TokenType.STRING_LITERAL, "\"NUMBER\"", Span(Position(1, 25, 24), Position(1, 34, 33))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 34, 33), Position(1, 35, 34))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 35, 34), Position(1, 36, 35))),
            ),
        )

        TestSnippetRegistry.register(
            "readInput-expression",
            listOf(
                Token(
                    TokenType.LET,
                    "let",
                    Span(
                        Position(1, 1, 0),
                        Position(1, 4, 3),
                    ),
                ),
                Token(TokenType.IDENTIFIER, "name", Span(Position(1, 5, 4), Position(1, 9, 8))),
                Token(TokenType.COLON, ":", Span(Position(1, 9, 8), Position(1, 10, 9))),
                Token(TokenType.TYPE_STRING, "string", Span(Position(1, 11, 10), Position(1, 17, 16))),
                Token(TokenType.ASSIGNMENT_OPERATOR, "=", Span(Position(1, 18, 17), Position(1, 19, 18))),
                Token(TokenType.READ_INPUT, "readInput", Span(Position(1, 20, 19), Position(1, 29, 28))),
                Token(TokenType.LEFT_PARENTHESIS, "(", Span(Position(1, 29, 28), Position(1, 30, 29))),
                Token(TokenType.STRING_LITERAL, "\"Enter your name: \"", Span(Position(1, 30, 29), Position(1, 50, 49))),
                Token(TokenType.RIGHT_PARENTHESIS, ")", Span(Position(1, 50, 49), Position(1, 51, 50))),
                Token(TokenType.SEMICOLON, ";", Span(Position(1, 51, 50), Position(1, 52, 51))),
            ),
        )
    }
}

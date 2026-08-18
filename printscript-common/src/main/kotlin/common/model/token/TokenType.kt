package common.model.token

enum class TokenType {

    // Keywords
    LET,
    PRINTLN,

    // Types
    TYPE_NUMBER,
    TYPE_STRING,

    IDENTIFIER,

    // Literals
    NUMBER_LITERAL,
    STRING_LITERAL,

    // Operators
    ASSIGNMENT_OPERATOR,
    ADDITION_OPERATOR,
    SUBTRACTION_OPERATOR,
    MULTIPLICATION_OPERATOR,
    DIVISION_OPERATOR,

    // Delimiters
    COLON,
    SEMICOLON,
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS,

    EOF,
}

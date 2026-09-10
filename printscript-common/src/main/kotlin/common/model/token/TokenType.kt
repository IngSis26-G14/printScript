package common.model.token

enum class TokenType {

    // Keywords
    LET,
    PRINTLN,
    IF,
    ELSE,
    CONST,
    READ_INPUT,
    READ_ENV,

    // Types
    TYPE_NUMBER,
    TYPE_STRING,
    TYPE_BOOLEAN,

    IDENTIFIER,

    // Literals
    NUMBER_LITERAL,
    STRING_LITERAL,
    BOOLEAN_LITERAL,

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
    LEFT_BRACE,
    RIGHT_BRACE,
}

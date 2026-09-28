package lexer.table

import common.model.token.TokenType
import common.type.option.Option
import lexer.rules.LexerRule
import lexer.rules.NumberRule
import lexer.rules.StringRule
import lexer.rules.SymbolRule
import lexer.rules.WordRule

internal object PrintScriptV10 : RuleTable {
    val reservedWords: Map<String, TokenType> = mapOf(
        "let" to TokenType.LET,
        "println" to TokenType.PRINTLN,
        "number" to TokenType.TYPE_NUMBER,
        "string" to TokenType.TYPE_STRING,
    )

    val symbols: Map<Char, TokenType> = mapOf(
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

    override val rules: List<LexerRule> = listOf(
        StringRule(),
        NumberRule(),
        WordRule(reservedWords),
        SymbolRule(symbols),
    )
}

internal object PrintScriptV11 : RuleTable {
    override val rules: List<LexerRule> = listOf(
        StringRule(),
        NumberRule(),
        WordRule(
            PrintScriptV10.reservedWords + mapOf(
                "const" to TokenType.CONST,
                "boolean" to TokenType.TYPE_BOOLEAN,
                "true" to TokenType.BOOLEAN_LITERAL,
                "false" to TokenType.BOOLEAN_LITERAL,
                "if" to TokenType.IF,
                "else" to TokenType.ELSE,
                "readInput" to TokenType.READ_INPUT,
                "readEnv" to TokenType.READ_ENV,
            ),
        ),
        SymbolRule(
            PrintScriptV10.symbols + mapOf(
                '{' to TokenType.LEFT_BRACE,
                '}' to TokenType.RIGHT_BRACE,
            ),
        ),
    )
}

internal object RuleTableRegistry {
    private val tables: Map<String, Lazy<RuleTable>> = mapOf(
        "1.0" to lazy { PrintScriptV10 },
        "1.1" to lazy { PrintScriptV11 },
    )

    fun get(version: String): Option<RuleTable> {
        val lazy = tables[version.lowercase()]
        return if (lazy != null) Option.Some(lazy.value) else Option.None
    }
}

package lexer.table

import lexer.rules.LexerRule

internal interface RuleTable {
    val rules: List<LexerRule>
}
package lexer.table

import common.type.option.Option
import lexer.rules.LexerRule
import lexer.rules.NumberRule
import lexer.rules.StringRule
import lexer.rules.SymbolRule
import lexer.rules.WordRule

internal object PrintScriptV10 : RuleTable {
    override val rules: List<LexerRule> = listOf(
        StringRule(),
        NumberRule(),
        WordRule(),
        SymbolRule(),
    )
}

internal object RuleTableRegistry {
    private val tables: Map<String, Lazy<RuleTable>> = mapOf(
        "1.0" to lazy { PrintScriptV10 },
    )

    fun get(version: String): Option<RuleTable> {
        val lazy = tables[version.lowercase()]
        return if (lazy != null) Option.Some(lazy.value) else Option.None
    }
}

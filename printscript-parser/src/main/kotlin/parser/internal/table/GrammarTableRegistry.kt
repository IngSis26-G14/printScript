package parser.internal.table

import common.type.option.Option

internal object GrammarTableRegistry {
    private val tables: Map<String, Lazy<GrammarTable>> = mapOf(
        "1.0" to lazy { PrintScriptV10 },
    )

    fun get(version: String): Option<GrammarTable> {
        val lazy = tables[version.lowercase()]
        return if (lazy != null) {
            Option.Some(lazy.value)
        } else {
            Option.None
        }
    }
}

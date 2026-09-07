package validator.util

import common.type.option.Option
import validator.model.symbol.StaticSymbol

internal class DefaultStaticSymbolTable private constructor(
    private val table: Map<String, StaticSymbol>,
) : StaticSymbolTable {

    constructor() : this(emptyMap())

    override fun contains(key: String): Boolean = table.containsKey(key)

    override fun get(key: String): Option<StaticSymbol> =
        table[key]?.let { Option.Some(it) } ?: Option.None

    override fun set(key: String, info: StaticSymbol): StaticSymbolTable {
        return DefaultStaticSymbolTable(table + (key to info))
    }
}

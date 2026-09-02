package validator.util

import common.type.option.Option
import validator.model.symbol.StaticSymbol

internal interface StaticSymbolTable {
    fun contains(key: String): Boolean
    fun get(key: String): Option<StaticSymbol>
    fun set(key: String, info: StaticSymbol): StaticSymbolTable
}

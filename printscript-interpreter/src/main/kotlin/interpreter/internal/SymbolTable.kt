package interpreter.internal

import common.model.value.Value
import common.model.value.type.ValueType

internal data class Variable(
    val declaredType: ValueType,
    val value: Value?,
    val isMutable: Boolean,
)

internal interface SymbolTable {
    fun find(name: String): Variable?

    fun declare(name: String, type: ValueType, value: Value?, isMutable: Boolean = true): Boolean

    fun assign(name: String, value: Value): Boolean
}

internal class InMemorySymbolTable : SymbolTable {
    private val variables = mutableMapOf<String, Variable>()

    override fun find(name: String): Variable? = variables[name]

    override fun declare(name: String, type: ValueType, value: Value?, isMutable: Boolean): Boolean {
        if (name in variables) return false
        variables[name] = Variable(type, value, isMutable)
        return true
    }

    override fun assign(name: String, value: Value): Boolean {
        val current = variables[name] ?: return false
        if (!current.isMutable) return false
        variables[name] = current.copy(value = value)
        return true
    }
}

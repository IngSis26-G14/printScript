package validator.table

import common.model.visitor.context.ContextVisitorTable
import common.type.option.Option

internal object VisitorTableRegistry {
    private val tables: Map<String, Lazy<ContextVisitorTable>> = mapOf(
        "1.0" to lazy { PrintScriptV10 },
        "1.1" to lazy { PrintScriptV11 },
    )

    fun get(version: String): Option<ContextVisitorTable> {
        val lazy = tables[version.lowercase()]
        return if (lazy != null) {
            Option.Some(lazy.value)
        } else {
            Option.None
        }
    }
}

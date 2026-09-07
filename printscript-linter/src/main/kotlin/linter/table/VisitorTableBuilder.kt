package linter.table

import common.model.diagnostic.Diagnostic
import common.model.rule.Rule
import common.model.visitor.Visitor
import common.model.visitor.VisitorTable
import common.type.outcome.Outcome
import linter.model.error.ConfigurationError

internal interface VisitorTableBuilder {

    class DefaultVisitorTable(override val visitors: Collection<Visitor>) : VisitorTable

    val factories: Map<String, VisitorFactory>

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun build(rules: Collection<Rule>): Outcome<VisitorTable, Diagnostic> {
        val visitors = mutableListOf<Visitor>()

        for (rule in rules) {
            val factory = factories[rule.signature] ?: continue
            try {
                visitors.add(factory.create(rule))
            } catch (e: ClassCastException) {
                val type = rule.value.type()
                val message = "Rule '${rule.signature}' value has invalid type '$type'"
                return Outcome.Error(ConfigurationError(message))
            }
        }

        return Outcome.Ok(DefaultVisitorTable(visitors))
    }
}

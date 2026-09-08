package formatter.table

import common.model.diagnostic.Diagnostic
import common.model.rule.Rule
import common.model.visitor.context.ContextVisitor
import common.type.outcome.Outcome
import formatter.model.error.ConfigurationError
import formatter.visitor.factory.ContextVisitorFactory

internal interface ContextVisitorTableBuilder {
    val factories: Map<String, ContextVisitorFactory>

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun build(rules: Collection<Rule>): Outcome<FormatterContextVisitorTable, Diagnostic> {
        val visitors = mutableListOf<ContextVisitor>()

        for (rule in rules) {
            val factory = factories[rule.signature] ?: continue
            try {
                visitors.add(factory.create(rule))
            } catch (e: Exception) {
                val type = rule.value.type()
                val message = "Rule '${rule.signature}' value has invalid type '$type'"
                return Outcome.Error(ConfigurationError(message))
            }
        }

        return Outcome.Ok(FormatterContextVisitorTable(visitors))
    }
}

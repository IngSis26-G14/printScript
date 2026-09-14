package formatter.table

import common.model.diagnostic.Diagnostic
import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.visitor.context.ContextVisitor
import common.type.outcome.Outcome
import formatter.model.error.ConfigurationError
import formatter.rule.NoSpacingAroundEqualsRule
import formatter.rule.SpacingAroundEqualsRule
import formatter.visitor.factory.ContextVisitorFactory

internal interface ContextVisitorTableBuilder {
    val factories: Map<String, ContextVisitorFactory>
    val defaults: List<Rule>
    val mandatory: Set<String>

    fun build(rules: Collection<Rule>): Outcome<FormatterContextVisitorTable, Diagnostic> {
        val configured = rules.associateBy { it.signature }.toMutableMap()
        if (configured.size != rules.size) return error("Duplicate formatter rule")
        normalizeSpacing(configured)?.let { return Outcome.Error(it) }
        for ((signature, rule) in configured) {
            if (signature !in factories) return error("Unknown formatter rule '$signature' for this version")
            if (signature in mandatory && rule.value != BooleanRuleValue(true)) {
                return error("Rule '$signature' is mandatory and cannot be disabled")
            }
        }

        val visitors = mutableListOf<ContextVisitor>()
        // Defaults define execution order; JSON property order does not change the result.
        for (default in defaults) {
            val rule = configured[default.signature] ?: default
            try {
                visitors.add(factories.getValue(rule.signature).create(rule))
            } catch (_: ClassCastException) {
                return error("Rule '${rule.signature}' has invalid value type '${rule.value.type()}'")
            } catch (exception: IllegalArgumentException) {
                return error(exception.message ?: "Invalid value for '${rule.signature}'")
            }
        }
        return Outcome.Ok(FormatterContextVisitorTable(visitors))
    }

    private fun normalizeSpacing(configured: MutableMap<String, Rule>): Diagnostic? {
        val noSpacing = configured.remove(NoSpacingAroundEqualsRule.signature) ?: return null
        val value = noSpacing.value as? BooleanRuleValue
            ?: return ConfigurationError("Rule '${noSpacing.signature}' requires a boolean")
        val equivalent = Rule(SpacingAroundEqualsRule.signature, BooleanRuleValue(!value.value))
        val existing = configured[equivalent.signature]
        if (existing != null && existing.value != equivalent.value) return ConfigurationError("Conflicting equals spacing rules")
        configured[equivalent.signature] = equivalent
        return null
    }

    private fun error(message: String): Outcome.Error<Diagnostic> = Outcome.Error(ConfigurationError(message))
}

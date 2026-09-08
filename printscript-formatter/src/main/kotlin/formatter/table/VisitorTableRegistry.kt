package formatter.table

import common.model.diagnostic.Diagnostic
import common.model.rule.Rule
import common.model.visitor.context.ContextVisitorTable
import common.type.outcome.Outcome
import formatter.model.error.ConfigurationError

internal object VisitorTableRegistry {

    private val builders: Map<String, Lazy<ContextVisitorTableBuilder>> = mapOf(
        "1.0" to lazy { PrintScriptV10 },
        "1.1" to lazy { PrintScriptV11 },
    )

    fun get(
        version: String,
        rules: Collection<Rule>,
    ): Outcome<ContextVisitorTable, Diagnostic> {
        val error = ConfigurationError("Unsupported language version '$version'")
        val lazyBuilder = builders[version.lowercase()] ?: return Outcome.Error(error)

        val builder = lazyBuilder.value
        return when (val visitorsOutcome = builder.build(rules)) {
            is Outcome.Ok -> {
                Outcome.Ok(visitorsOutcome.value)
            }
            is Outcome.Error -> Outcome.Error(visitorsOutcome.error)
        }
    }
}

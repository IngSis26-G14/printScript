package common.model.visitor.context

import common.model.diagnostic.Diagnostic
import common.model.value.Value
import common.type.outcome.Outcome

data class VisitResult(
    val outcome: Outcome<Value, Diagnostic>,
    val context: VisitorContext,
)

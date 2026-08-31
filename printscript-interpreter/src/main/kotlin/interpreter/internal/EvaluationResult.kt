package interpreter.internal

import common.model.diagnostic.Diagnostic
import common.model.value.Value

internal sealed interface EvaluationResult {
    data class Success(val value: Value) : EvaluationResult

    data class Failure(val diagnostic: Diagnostic) : EvaluationResult
}

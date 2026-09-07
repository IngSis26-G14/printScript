package common.model.value.operation

import common.model.value.Value

sealed interface OperationResult {
    data class Ok(val value: Value) : OperationResult
    data class Error(val message: String) : OperationResult
    data object Unsupported : OperationResult
}

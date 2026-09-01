package common.model.value.operation

import common.model.value.Value
import common.model.value.type.ValueType

interface UnaryValueOperation {
    val symbol: String
    val resultType: ValueType
    fun supports(type: ValueType): Boolean
    fun apply(operand: Value): OperationResult
}

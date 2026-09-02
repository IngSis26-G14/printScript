package common.model.value.operation

import common.model.value.Value
import common.model.value.type.ValueType

interface BinaryValueOperation {
    val symbol: String
    val resultType: ValueType
    fun supports(lhs: ValueType, rhs: ValueType): Boolean
    fun apply(lhs: Value, rhs: Value): OperationResult
}

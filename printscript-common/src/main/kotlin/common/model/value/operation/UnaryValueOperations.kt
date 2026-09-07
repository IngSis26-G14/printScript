package common.model.value.operation

import common.model.value.FloatValue
import common.model.value.IntegerValue
import common.model.value.Value
import common.model.value.type.NumberValueType
import common.model.value.type.ValueType

object NegateIntegerOperation : UnaryValueOperation {
    override val symbol = "-"
    override val resultType = NumberValueType

    override fun supports(type: ValueType): Boolean {
        return type == NumberValueType
    }

    override fun apply(operand: Value): OperationResult {
        return when (operand) {
            is IntegerValue -> {
                val value = IntegerValue(-operand.value)
                OperationResult.Ok(value)
            }
            else -> OperationResult.Unsupported
        }
    }
}

object NegateFloatOperation : UnaryValueOperation {
    override val symbol = "-"
    override val resultType = NumberValueType

    override fun supports(type: ValueType): Boolean {
        return type == NumberValueType
    }

    override fun apply(operand: Value): OperationResult {
        return when (operand) {
            is FloatValue -> {
                val value = FloatValue(-operand.value)
                OperationResult.Ok(value)
            }
            else -> OperationResult.Unsupported
        }
    }
}

object PositiveIntegerOperation : UnaryValueOperation {
    override val symbol = "+"
    override val resultType = NumberValueType

    override fun supports(type: ValueType): Boolean {
        return type == NumberValueType
    }

    override fun apply(operand: Value): OperationResult {
        return when (operand) {
            is IntegerValue -> OperationResult.Ok(operand)
            else -> OperationResult.Unsupported
        }
    }
}

object PositiveFloatOperation : UnaryValueOperation {
    override val symbol = "+"
    override val resultType = NumberValueType

    override fun supports(type: ValueType): Boolean {
        return type == NumberValueType
    }

    override fun apply(operand: Value): OperationResult {
        return when (operand) {
            is FloatValue -> OperationResult.Ok(operand)
            else -> OperationResult.Unsupported
        }
    }
}

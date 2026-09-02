package common.model.value.operation

import common.model.value.FloatValue
import common.model.value.IntegerValue
import common.model.value.StringValue
import common.model.value.Value
import common.model.value.type.BooleanValueType
import common.model.value.type.NumberValueType
import common.model.value.type.StringValueType
import common.model.value.type.ValueType

object AddIntegerOperation : BinaryValueOperation {
    override val symbol = "+"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is IntegerValue && rhs is IntegerValue -> {
                OperationResult.Ok(IntegerValue(lhs.value + rhs.value))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object AddFloatOperation : BinaryValueOperation {
    override val symbol = "+"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is FloatValue && rhs is FloatValue -> {
                OperationResult.Ok(FloatValue(lhs.value + rhs.value))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object AddMixedNumericOperation : BinaryValueOperation {
    override val symbol = "+"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is IntegerValue && rhs is FloatValue -> {
                OperationResult.Ok(FloatValue(lhs.value.toFloat() + rhs.value))
            }
            lhs is FloatValue && rhs is IntegerValue -> {
                OperationResult.Ok(FloatValue(lhs.value + rhs.value.toFloat()))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object AddStringOperation : BinaryValueOperation {
    override val symbol = "+"
    override val resultType = StringValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == StringValueType && rhs == StringValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is StringValue && rhs is StringValue -> {
                OperationResult.Ok(StringValue(lhs.value + rhs.value))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object AddStringCoercionOperation : BinaryValueOperation {
    override val symbol = "+"
    override val resultType = StringValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return (lhs != BooleanValueType && rhs != BooleanValueType) &&
            (lhs == StringValueType || rhs == StringValueType)
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is StringValue -> {
                OperationResult.Ok(StringValue(lhs.value + rhs.format()))
            }
            rhs is StringValue -> {
                OperationResult.Ok(StringValue(lhs.format() + rhs.value))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object SubtractIntegerOperation : BinaryValueOperation {
    override val symbol = "-"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is IntegerValue && rhs is IntegerValue -> {
                OperationResult.Ok(IntegerValue(lhs.value - rhs.value))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object SubtractFloatOperation : BinaryValueOperation {
    override val symbol = "-"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is FloatValue && rhs is FloatValue -> {
                OperationResult.Ok(FloatValue(lhs.value - rhs.value))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object SubtractMixedNumericOperation : BinaryValueOperation {
    override val symbol = "-"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is IntegerValue && rhs is FloatValue -> {
                OperationResult.Ok(FloatValue(lhs.value.toFloat() - rhs.value))
            }
            lhs is FloatValue && rhs is IntegerValue -> {
                OperationResult.Ok(FloatValue(lhs.value - rhs.value.toFloat()))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object MultiplyIntegerOperation : BinaryValueOperation {
    override val symbol = "*"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is IntegerValue && rhs is IntegerValue -> {
                OperationResult.Ok(IntegerValue(lhs.value * rhs.value))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object MultiplyFloatOperation : BinaryValueOperation {
    override val symbol = "*"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is FloatValue && rhs is FloatValue -> {
                OperationResult.Ok(FloatValue(lhs.value * rhs.value))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object MultiplyMixedNumericOperation : BinaryValueOperation {
    override val symbol = "*"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is IntegerValue && rhs is FloatValue -> {
                OperationResult.Ok(FloatValue(lhs.value.toFloat() * rhs.value))
            }
            lhs is FloatValue && rhs is IntegerValue -> {
                OperationResult.Ok(FloatValue(lhs.value * rhs.value.toFloat()))
            }
            else -> OperationResult.Unsupported
        }
    }
}

object DivideIntegerOperation : BinaryValueOperation {
    override val symbol = "/"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is IntegerValue && rhs is IntegerValue -> {
                if (rhs.value == 0) {
                    OperationResult.Error("Division by zero")
                } else {
                    OperationResult.Ok(IntegerValue(lhs.value / rhs.value))
                }
            }
            else -> OperationResult.Unsupported
        }
    }
}

object DivideFloatOperation : BinaryValueOperation {
    override val symbol = "/"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is FloatValue && rhs is FloatValue -> {
                if (rhs.value == 0f) {
                    OperationResult.Error("Division by zero")
                } else {
                    OperationResult.Ok(FloatValue(lhs.value / rhs.value))
                }
            }
            else -> OperationResult.Unsupported
        }
    }
}

object DivideMixedNumericOperation : BinaryValueOperation {
    override val symbol = "/"
    override val resultType = NumberValueType

    override fun supports(lhs: ValueType, rhs: ValueType): Boolean {
        return lhs == NumberValueType && rhs == NumberValueType
    }

    override fun apply(lhs: Value, rhs: Value): OperationResult {
        return when {
            lhs is IntegerValue && rhs is FloatValue -> {
                if (rhs.value == 0f) {
                    OperationResult.Error("Division by zero")
                } else {
                    OperationResult.Ok(FloatValue(lhs.value.toFloat() / rhs.value))
                }
            }
            lhs is FloatValue && rhs is IntegerValue -> {
                if (rhs.value == 0) {
                    OperationResult.Error("Division by zero")
                } else {
                    OperationResult.Ok(FloatValue(lhs.value / rhs.value.toFloat()))
                }
            }
            else -> OperationResult.Unsupported
        }
    }
}

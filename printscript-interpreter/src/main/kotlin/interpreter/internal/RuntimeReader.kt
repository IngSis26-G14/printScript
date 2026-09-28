package interpreter.internal

import common.io.reader.env.EnvReader
import common.io.reader.input.InputReader
import common.io.writer.OutputWriter
import common.model.node.Node
import common.model.node.ReadInputExpressionNode
import common.model.value.BooleanValue
import common.model.value.FloatValue
import common.model.value.IntegerValue
import common.model.value.StringValue
import common.model.value.Value
import common.model.value.type.BooleanValueType
import common.model.value.type.NumberValueType
import common.model.value.type.StringValueType
import common.model.value.type.ValueType
import common.type.option.Option
import interpreter.internal.diagnostic.InterpreterDiagnostic
import interpreter.internal.diagnostic.Runtime

internal class RuntimeReader(
    private val input: InputReader,
    private val output: OutputWriter,
    private val env: EnvReader,
) {
    fun read(node: Node, argument: String, expectedType: ValueType): EvaluationResult {
        val raw = if (node.type == ReadInputExpressionNode) {
            output.write(sequenceOf(argument))
            input.read().joinToString("").removeSuffix("\n").removeSuffix("\r")
        } else {
            when (val result = env.read(argument)) {
                is Option.Some -> result.value.format()
                is Option.None -> return failure("Environment variable '$argument' is not defined", node)
            }
        }
        val value = convert(raw, expectedType)
            ?: return failure("Cannot convert read value to '${expectedType.name}'", node)
        return EvaluationResult.Success(value)
    }

    private fun convert(raw: String, expectedType: ValueType): Value? = when (expectedType) {
        StringValueType -> StringValue(raw)
        BooleanValueType -> raw.trim().toBooleanStrictOrNull()?.let(::BooleanValue)
        NumberValueType -> number(raw.trim())
        else -> null
    }

    private fun number(raw: String): Value? {
        raw.toIntOrNull()?.let { return IntegerValue(it) }
        return raw.toFloatOrNull()?.takeIf { it.isFinite() }?.let(::FloatValue)
    }

    private fun failure(message: String, node: Node) =
        EvaluationResult.Failure(InterpreterDiagnostic(message, Runtime, node.span))
}

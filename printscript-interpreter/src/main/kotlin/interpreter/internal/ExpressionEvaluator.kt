package interpreter.internal

import common.model.node.BinaryOperationExpressionNode
import common.model.node.BooleanLiteralNode
import common.model.node.DivideNode
import common.model.node.IdentifierNode
import common.model.node.MinusNode
import common.model.node.MultiplyNode
import common.model.node.Node
import common.model.node.NumberLiteralNode
import common.model.node.ParenthesizedExpressionNode
import common.model.node.PlusNode
import common.model.node.ReadEnvExpressionNode
import common.model.node.ReadInputExpressionNode
import common.model.node.StringLiteralNode
import common.model.node.UnaryOperationNode
import common.model.span.Span
import common.model.value.FloatValue
import common.model.value.IntegerValue
import common.model.value.StringValue
import common.model.value.Value
import common.model.value.type.NumberValueType
import common.model.value.type.StringValueType
import common.model.value.type.ValueType
import interpreter.internal.diagnostic.InterpreterDiagnostic
import interpreter.internal.diagnostic.Runtime

internal class ExpressionEvaluator(
    private val symbols: SymbolTable,
    private val reader: RuntimeReader,
) {
    fun evaluate(node: Node, expectedType: ValueType = StringValueType): EvaluationResult = when (node.type) {
        NumberLiteralNode, BooleanLiteralNode -> literal(node)
        StringLiteralNode -> stringLiteral(node)
        IdentifierNode -> identifier(node)
        ParenthesizedExpressionNode -> parenthesized(node, expectedType)
        UnaryOperationNode -> unary(node)
        BinaryOperationExpressionNode -> binary(node, expectedType)
        ReadInputExpressionNode, ReadEnvExpressionNode -> read(node, expectedType)
        else -> failure("Unsupported expression '${node.type}'", span = node.span)
    }

    private fun read(node: Node, expectedType: ValueType): EvaluationResult {
        val children = (node as? Node.Composite)?.children?.toList() ?: return malformed(node)
        val argument = children.getOrNull(2) ?: return malformed(node)
        val result = evaluate(argument, StringValueType)
        if (result is EvaluationResult.Failure) return result
        val value = (result as EvaluationResult.Success).value
        if (value !is StringValue) return failure("Read argument must be a string", span = argument.span)
        return reader.read(node, value.value, expectedType)
    }

    private fun literal(node: Node): EvaluationResult {
        val leaf = node as? Node.Leaf ?: return malformed(node)
        return EvaluationResult.Success(leaf.value)
    }

    private fun stringLiteral(node: Node): EvaluationResult {
        val leaf = node as? Node.Leaf ?: return malformed(node)
        val raw = (leaf.value as? StringValue)?.value ?: return malformed(node)
        val value = if (raw.length >= 2 && raw.first() == raw.last() && raw.first() in "\"'") {
            raw.substring(1, raw.lastIndex)
        } else {
            raw
        }
        return EvaluationResult.Success(StringValue(value))
    }

    private fun identifier(node: Node): EvaluationResult {
        val name = leafText(node) ?: return malformed(node)
        val variable = symbols.find(name)
            ?: return failure("Variable '$name' is not declared", span = node.span)
        return variable.value?.let(EvaluationResult::Success)
            ?: failure("Variable '$name' has not been initialized", span = node.span)
    }

    private fun parenthesized(node: Node, expectedType: ValueType): EvaluationResult {
        val composite = node as? Node.Composite ?: return malformed(node)
        val inner = composite.children.firstOrNull {
            it.type != common.model.node.LeftParenthesisNode && it.type != common.model.node.RightParenthesisNode
        } ?: return malformed(node)
        return evaluate(inner, expectedType)
    }

    private fun unary(node: Node): EvaluationResult {
        val children = (node as? Node.Composite)?.children?.toList() ?: return malformed(node)
        if (children.size != 2) return malformed(node)
        val operand = evaluate(children[1], NumberValueType)
        if (operand is EvaluationResult.Failure) return operand
        val value = (operand as EvaluationResult.Success).value
        return when (children[0].type) {
            PlusNode -> numericUnary(value, 1, children[1].span)
            MinusNode -> numericUnary(value, -1, children[1].span)
            else -> failure("Unsupported unary operator", span = children[0].span)
        }
    }

    private fun numericUnary(value: Value, sign: Int, span: Span): EvaluationResult = when (value) {
        is IntegerValue -> EvaluationResult.Success(IntegerValue(value.value * sign))
        is FloatValue -> EvaluationResult.Success(FloatValue(value.value * sign))
        else -> failure("Unary operators require a number, got '${value.type.name}'", span = span)
    }

    private fun binary(node: Node, expectedType: ValueType): EvaluationResult {
        val children = (node as? Node.Composite)?.children?.toList() ?: return malformed(node)
        if (children.size != 3) return malformed(node)
        val operandType = if (children[1].type == PlusNode) expectedType else NumberValueType
        val left = evaluate(children[0], operandType)
        if (left is EvaluationResult.Failure) return left
        val right = evaluate(children[2], operandType)
        if (right is EvaluationResult.Failure) return right
        return applyBinary(
            (left as EvaluationResult.Success).value,
            children[1],
            (right as EvaluationResult.Success).value,
        )
    }

    private fun applyBinary(left: Value, operator: Node, right: Value): EvaluationResult {
        if (operator.type == PlusNode && (left is StringValue || right is StringValue)) {
            return EvaluationResult.Success(StringValue(left.format() + right.format()))
        }
        val leftNumber = numberValue(left)
        val rightNumber = numberValue(right)
        if (leftNumber == null || rightNumber == null) {
            return failure(
                "Operator '${leafText(operator)}' requires numeric operands",
                span = operator.span,
            )
        }
        if (operator.type == DivideNode && rightNumber == 0f) {
            return failure("Division by zero", Runtime, operator.span)
        }

        val result = calculate(leftNumber, operator, rightNumber)
            ?: return failure("Unsupported binary operator", span = operator.span)
        val bothIntegers = left is IntegerValue && right is IntegerValue
        val keepInteger = bothIntegers && operator.type != DivideNode
        return EvaluationResult.Success(if (keepInteger) IntegerValue(result.toInt()) else FloatValue(result))
    }

    private fun numberValue(value: Value): Float? = when (value) {
        is IntegerValue -> value.value.toFloat()
        is FloatValue -> value.value
        else -> null
    }

    private fun calculate(left: Float, operator: Node, right: Float): Float? =
        when (operator.type) {
            PlusNode -> left + right
            MinusNode -> left - right
            MultiplyNode -> left * right
            DivideNode -> left / right
            else -> null
        }

    private fun leafText(node: Node): String? = ((node as? Node.Leaf)?.value as? StringValue)?.value

    private fun malformed(node: Node) = failure("Malformed '${node.type}' node", span = node.span)

    private fun failure(
        message: String,
        category: common.model.diagnostic.category.Category = interpreter.internal.diagnostic.Semantic,
        span: Span? = null,
    ) = EvaluationResult.Failure(InterpreterDiagnostic(message, category, span))
}

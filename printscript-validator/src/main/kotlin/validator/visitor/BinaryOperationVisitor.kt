package validator.visitor

import common.model.node.BinaryOperationExpressionNode
import common.model.node.Node
import common.model.node.NodeType
import common.model.span.Span
import common.model.value.NoneValue
import common.model.value.Value
import common.model.value.operation.BinaryValueOperation
import common.model.value.operation.OperationResult
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import validator.model.category.InvalidValue
import validator.model.category.TypeMismatch
import validator.model.category.UnsupportedOperator
import validator.model.error.ValidationError
import validator.model.value.RuntimeValue

internal class BinaryOperationVisitor(
    private val operations: Map<NodeType, Collection<BinaryValueOperation>>,
) : ContextVisitor {

    override fun visit(
        node: Node.Leaf,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult = VisitResult(Outcome.Ok(NoneValue), context)

    override fun visit(
        node: Node.Composite,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult {
        if (node.type != BinaryOperationExpressionNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val children = node.children.toList()
        val lhsNode = children[0]
        val operatorNode = children[1]
        val rhsNode = children[2]

        val lhsVisit = table.dispatch(lhsNode, context)
        if (lhsVisit.outcome is Outcome.Error) return lhsVisit

        val rhsVisit = table.dispatch(rhsNode, lhsVisit.context)
        if (rhsVisit.outcome is Outcome.Error) return rhsVisit

        val lhsValue = (lhsVisit.outcome as Outcome.Ok).value
        val rhsValue = (rhsVisit.outcome as Outcome.Ok).value

        val operatorType = operatorNode.type
        val symbol = (operatorNode as Node.Leaf).value.format()

        if (!operations.containsKey(operatorType)) {
            return createUnsupportedOperatorError(symbol, operatorNode.span, rhsVisit.context)
        }

        if (lhsValue is RuntimeValue || rhsValue is RuntimeValue) {
            return handleRuntimeValues(
                lhsValue,
                rhsValue,
                operatorType,
                symbol,
                operatorNode,
                node,
                rhsVisit.context,
            )
        }

        return handleConcreteValues(
            lhsValue,
            rhsValue,
            operatorType,
            symbol,
            operatorNode,
            node,
            rhsVisit.context,
        )
    }

    private fun handleRuntimeValues(
        lhsValue: Value,
        rhsValue: Value,
        operatorType: NodeType,
        symbol: String,
        operatorNode: Node,
        node: Node.Composite,
        context: VisitorContext,
    ): VisitResult {
        val lhsType = if (lhsValue is RuntimeValue) lhsValue.type else lhsValue.type
        val rhsType = if (rhsValue is RuntimeValue) rhsValue.type else rhsValue.type

        val ops = operations[operatorType]
            ?: return createUnsupportedOperatorError(symbol, operatorNode.span, context)

        for (op in ops) {
            if (op.supports(lhsType, rhsType)) {
                return VisitResult(Outcome.Ok(RuntimeValue(op.resultType)), context)
            }
        }

        return createTypeMismatchError(lhsType.name, rhsType.name, symbol, node.span, context)
    }

    private fun handleConcreteValues(
        lhsValue: Value,
        rhsValue: Value,
        operatorType: NodeType,
        symbol: String,
        operatorNode: Node,
        node: Node.Composite,
        context: VisitorContext,
    ): VisitResult {
        val ops = operations.getValue(operatorType)

        for (op in ops) {
            when (val result = op.apply(lhsValue, rhsValue)) {
                is OperationResult.Ok -> {
                    return VisitResult(Outcome.Ok(result.value), context)
                }
                is OperationResult.Error -> {
                    return createInvalidValueError(result.message, operatorNode.span, context)
                }
                is OperationResult.Unsupported -> continue
            }
        }

        val lhsType = lhsValue.type.name
        val rhsType = rhsValue.type.name
        return createTypeMismatchError(lhsType, rhsType, symbol, node.span, context)
    }

    private fun createUnsupportedOperatorError(
        symbol: String,
        span: Span,
        context: VisitorContext,
    ): VisitResult {
        val error = ValidationError(
            "Unsupported binary operator '$symbol'",
            UnsupportedOperator,
            span,
        )
        return VisitResult(Outcome.Error(error), context)
    }

    private fun createTypeMismatchError(
        lhsType: String,
        rhsType: String,
        symbol: String,
        span: Span,
        context: VisitorContext,
    ): VisitResult {
        val error = ValidationError(
            "Cannot apply operator '$symbol' to operands of type $lhsType and $rhsType",
            TypeMismatch,
            span,
        )
        return VisitResult(Outcome.Error(error), context)
    }

    private fun createInvalidValueError(
        message: String,
        span: Span,
        context: VisitorContext,
    ): VisitResult {
        val error = ValidationError(
            message,
            InvalidValue,
            span,
        )
        return VisitResult(Outcome.Error(error), context)
    }
}

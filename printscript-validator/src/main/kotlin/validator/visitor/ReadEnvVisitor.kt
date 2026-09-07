package validator.visitor

import common.model.node.LeftParenthesisNode
import common.model.node.Node
import common.model.node.ReadEnvExpressionNode
import common.model.node.RightParenthesisNode
import common.model.value.NoneValue
import common.model.value.StringValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import common.type.outcome.getOrElse
import validator.model.category.IncorrectMethodCall
import validator.model.error.ValidationError
import validator.model.value.RuntimeValue
import validator.model.value.RuntimeValueType

internal class ReadEnvVisitor : ContextVisitor {

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
        if (node.type != ReadEnvExpressionNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val children = node.children.toList()
        val arguments = children
            .dropWhile { it.type != LeftParenthesisNode }
            .drop(1)
            .takeWhile { it.type != RightParenthesisNode }

        if (arguments.isEmpty()) {
            val message = "readEnv() requires an argument"
            val error = ValidationError(message, IncorrectMethodCall, node.span)
            return VisitResult(Outcome.Error(error), context)
        }

        val argument = arguments[0]
        val visit = table.dispatch(argument, context)
        if (visit.outcome is Outcome.Error) return visit

        val value = visit.outcome.getOrElse { return visit }

        if (value is RuntimeValue) {
            return VisitResult(Outcome.Ok(RuntimeValue(RuntimeValueType)), visit.context)
        }

        if (value !is StringValue) {
            val message = "readEnv() argument must be a string, got ${value.type.name}"
            val error = ValidationError(message, IncorrectMethodCall, argument.span)
            return VisitResult(Outcome.Error(error), visit.context)
        }

        return VisitResult(Outcome.Ok(RuntimeValue(RuntimeValueType)), visit.context)
    }
}

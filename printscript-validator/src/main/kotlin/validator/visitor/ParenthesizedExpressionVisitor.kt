package validator.visitor

import common.model.node.LeftParenthesisNode
import common.model.node.Node
import common.model.node.ParenthesizedExpressionNode
import common.model.node.RightParenthesisNode
import common.model.value.NoneValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome

internal class ParenthesizedExpressionVisitor : ContextVisitor {
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
        if (node.type != ParenthesizedExpressionNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val children = node.children.toList()
        val arguments = children
            .dropWhile { it.type != LeftParenthesisNode }
            .drop(1)
            .takeWhile { it.type != RightParenthesisNode }

        if (arguments.isEmpty()) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val argument = arguments[0]
        return table.dispatch(argument, context)
    }
}

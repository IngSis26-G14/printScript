package validator.visitor

import common.model.node.BlockNode
import common.model.node.LeftBraceNode
import common.model.node.Node
import common.model.node.RightBraceNode
import common.model.value.NoneValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome

internal class BlockVisitor : ContextVisitor {

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
        if (node.type != BlockNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val children = node.children.toList()
        val statements = children
            .dropWhile { it.type != LeftBraceNode }
            .drop(1)
            .takeWhile { it.type != RightBraceNode }

        var currentContext = context

        for (statement in statements) {
            val visit = table.dispatch(statement, currentContext)
            if (visit.outcome is Outcome.Error) return visit

            currentContext = visit.context
        }

        return VisitResult(Outcome.Ok(NoneValue), currentContext)
    }
}

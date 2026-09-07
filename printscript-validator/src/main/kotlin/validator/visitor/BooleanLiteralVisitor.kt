package validator.visitor

import common.model.node.BooleanLiteralNode
import common.model.node.Node
import common.model.value.NoneValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome

internal class BooleanLiteralVisitor : ContextVisitor {

    override fun visit(
        node: Node.Leaf,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult {
        if (node.type != BooleanLiteralNode) return VisitResult(Outcome.Ok(NoneValue), context)

        val value = node.value
        return VisitResult(Outcome.Ok(value), context)
    }

    override fun visit(
        node: Node.Composite,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult = VisitResult(Outcome.Ok(NoneValue), context)
}

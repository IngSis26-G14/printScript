package validator.visitor

import common.model.node.Node
import common.model.node.StringLiteralNode
import common.model.value.NoneValue
import common.model.value.StringValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome

internal class StringLiteralVisitor : ContextVisitor {

    override fun visit(
        node: Node.Leaf,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult {
        if (node.type != StringLiteralNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        return when (val value = node.value) {
            is StringValue -> {
                val unquoted = value.value
                    .removeSurrounding("\"")
                    .removeSurrounding("'")
                VisitResult(Outcome.Ok(StringValue(unquoted)), context)
            }
            else -> {
                VisitResult(Outcome.Ok(NoneValue), context)
            }
        }
    }

    override fun visit(
        node: Node.Composite,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult = VisitResult(Outcome.Ok(NoneValue), context)
}

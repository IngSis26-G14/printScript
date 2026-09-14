package formatter.table

import common.model.node.Node
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import formatter.model.value.NodeValue
import formatter.transformer.NodeTransformer

internal class FormatterContextVisitorTable(
    override val visitors: Collection<ContextVisitor>,
) : ContextVisitorTable {
    override fun dispatch(node: Node, context: VisitorContext): VisitResult {
        var currentContext = context
        var currentNode = node
        for (visitor in visitors) {
            val visit = NodeTransformer.transformRecursive(currentNode, visitor, this, currentContext)
            when (val outcome = visit.outcome) {
                is Outcome.Error -> return visit
                is Outcome.Ok -> currentNode = (outcome.value as NodeValue).value
            }
            currentContext = visit.context
        }
        return VisitResult(Outcome.Ok(NodeValue(currentNode)), currentContext)
    }
}

package formatter.table

import common.model.node.Node
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import formatter.model.value.DocValue
import formatter.model.value.NodeValue
import formatter.type.toDoc

internal class FormatterContextVisitorTable(
    override val visitors: Collection<ContextVisitor>,
) : ContextVisitorTable {

    override fun dispatch(node: Node, context: VisitorContext): VisitResult {
        var currentContext = context
        var currentNode = node

        for (visitor in visitors) {
            val visit = currentNode.accept(visitor, this, currentContext)
            val outcome = visit.outcome
            if (outcome is Outcome.Error) return visit

            currentContext = visit.context

            if (outcome is Outcome.Ok) {
                val value = outcome.value
                if (value is NodeValue) {
                    currentNode = value.value
                }
            }
        }

        val result = VisitResult(Outcome.Ok(DocValue(currentNode.toDoc())), currentContext)
        return result
    }
}

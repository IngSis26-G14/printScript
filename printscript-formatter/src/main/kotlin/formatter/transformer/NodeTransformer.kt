package formatter.transformer

import common.model.node.Node
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import formatter.model.value.NodeValue

internal object NodeTransformer {
    /** Applies one rule bottom-up, retaining transformed children and context. */
    fun transformRecursive(
        node: Node,
        visitor: ContextVisitor,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult {
        var currentContext = context
        val updated = when (node) {
            is Node.Leaf -> node
            is Node.Composite -> {
                val children = mutableListOf<Node>()
                for (child in node.children) {
                    val result = transformRecursive(child, visitor, table, currentContext)
                    val outcome = result.outcome
                    if (outcome is Outcome.Error) return result
                    children.add(((outcome as Outcome.Ok).value as NodeValue).value)
                    currentContext = result.context
                }
                node.copy(children = children)
            }
        }
        val visit = updated.accept(visitor, table, currentContext)
        return when (val outcome = visit.outcome) {
            is Outcome.Error -> visit
            is Outcome.Ok -> {
                val value = outcome.value as? NodeValue ?: NodeValue(updated)
                VisitResult(Outcome.Ok(value), visit.context)
            }
        }
    }
}

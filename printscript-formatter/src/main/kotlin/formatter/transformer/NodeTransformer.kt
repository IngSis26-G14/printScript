package formatter.transformer

import common.model.node.Node
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import formatter.model.value.DocValue

internal object NodeTransformer {

    fun applyDispatchResult(child: Node, dispatchResult: VisitResult): Node {
        val outcome = dispatchResult.outcome

        return when (outcome) {
            is Outcome.Ok -> {
                val value = outcome.value
                if (value is DocValue) {
                    val doc = value.value
                    when (child) {
                        is Node.Leaf -> child.copy(leading = doc.leading, trailing = doc.trailing)
                        is Node.Composite -> child
                    }
                } else {
                    child
                }
            }
            else -> child
        }
    }

    fun transformRecursive(
        node: Node,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): Node {
        return when (node) {
            is Node.Leaf -> node
            is Node.Composite -> {
                val transformedChildren = node.children.map { child ->
                    val dispatched = applyDispatchResult(child, table.dispatch(child, context))
                    transformRecursive(dispatched, table, context)
                }
                node.copy(children = transformedChildren)
            }
        }
    }
}

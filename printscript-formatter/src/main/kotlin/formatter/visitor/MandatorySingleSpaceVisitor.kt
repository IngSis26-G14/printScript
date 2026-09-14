package formatter.visitor

import common.model.node.ConstNode
import common.model.node.IfNode
import common.model.node.LetNode
import common.model.node.Node
import common.model.trivia.NewlineTrivia
import common.model.trivia.SpaceTrivia
import common.model.trivia.TabTrivia
import common.model.trivia.Trivia
import common.model.value.NoneValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import formatter.model.value.NodeValue

internal class MandatorySingleSpaceVisitor(private val enforce: Boolean) : ContextVisitor {
    override fun visit(node: Node.Leaf, table: ContextVisitorTable, context: VisitorContext): VisitResult {
        if (!enforce) return VisitResult(Outcome.Ok(NoneValue), context)
        val whitespace = setOf(SpaceTrivia, TabTrivia, NewlineTrivia)
        val separator = if (node.type in setOf(LetNode, ConstNode, IfNode)) {
            listOf(Trivia(SpaceTrivia, " ", node.span))
        } else {
            emptyList()
        }
        val updated = node.copy(
            leading = node.leading.filter { it.type !in whitespace },
            trailing = node.trailing.filter { it.type !in whitespace } + separator,
        )
        return VisitResult(Outcome.Ok(NodeValue(updated)), context)
    }

    override fun visit(node: Node.Composite, table: ContextVisitorTable, context: VisitorContext): VisitResult =
        VisitResult(Outcome.Ok(NoneValue), context)
}

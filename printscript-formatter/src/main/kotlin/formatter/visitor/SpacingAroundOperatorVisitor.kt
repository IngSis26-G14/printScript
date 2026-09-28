package formatter.visitor

import common.model.node.Node
import common.model.node.NodeType
import common.model.trivia.SpaceTrivia
import common.model.trivia.Trivia
import common.model.value.NoneValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import formatter.manipulator.TriviaManipulator
import formatter.model.value.NodeValue

internal class SpacingAroundOperatorVisitor(
    private val enforce: Boolean,
    private val operators: Collection<NodeType>,
) : ContextVisitor {
    override fun visit(node: Node.Leaf, table: ContextVisitorTable, context: VisitorContext): VisitResult {
        if (node.type !in operators) return VisitResult(Outcome.Ok(NoneValue), context)
        val cleaned = TriviaManipulator.removeTrailing(
            TriviaManipulator.removeLeading(node, SpaceTrivia),
            SpaceTrivia,
        )
        val spaces = if (enforce) listOf(Trivia(SpaceTrivia, " ", node.span)) else emptyList()
        val updated = TriviaManipulator.addTrailing(TriviaManipulator.addLeading(cleaned, spaces), spaces)
        return VisitResult(Outcome.Ok(NodeValue(updated)), context)
    }

    override fun visit(node: Node.Composite, table: ContextVisitorTable, context: VisitorContext): VisitResult {
        // Adjacent unary/binary operators share a boundary: keep one space there.
        val children = node.children.toList()
        val updated = children.mapIndexed { index, child ->
            val previous = children.getOrNull(index - 1)
            if (previous?.type in operators) TriviaManipulator.removeLeading(child, SpaceTrivia) else child
        }
        return VisitResult(Outcome.Ok(NodeValue(node.copy(children = updated))), context)
    }
}

package formatter.visitor

import common.model.node.Node
import common.model.node.SemicolonNode
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

internal class MandatorySingleSpaceVisitor(
    private val enforce: Boolean,
) : ContextVisitor {

    override fun visit(
        node: Node.Leaf,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult {
        return VisitResult(Outcome.Ok(NoneValue), context)
    }

    override fun visit(
        node: Node.Composite,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult {
        if (!enforce) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val children = node.children.toList()
        if (children.size < 2) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val cleanedChildren = children.map { child ->
            val withoutTrailing = TriviaManipulator.removeTrailing(child, SpaceTrivia)
            TriviaManipulator.removeLeading(withoutTrailing, SpaceTrivia)
        }

        val finalChildren = cleanedChildren.mapIndexed { i, current ->
            val next = cleanedChildren.getOrNull(i + 1)

            if (next != null && !shouldHaveNoSpaceBefore(next)) {
                val space = listOf(Trivia(SpaceTrivia, " ", current.span))
                TriviaManipulator.addTrailing(current, space)
            } else {
                current
            }
        }

        val updatedNode = node.copy(children = finalChildren)
        return VisitResult(Outcome.Ok(NodeValue(updatedNode)), context)
    }

    private fun shouldHaveNoSpaceBefore(node: Node): Boolean {
        return when (node) {
            is Node.Leaf -> node.type is SemicolonNode
            is Node.Composite -> false
        }
    }
}

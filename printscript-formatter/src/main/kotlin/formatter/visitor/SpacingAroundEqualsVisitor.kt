package formatter.visitor

import common.model.node.AssignNode
import common.model.node.Node
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

internal class SpacingAroundEqualsVisitor(
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
        val assignIndex = children.indexOfFirst {
            it is Node.Leaf && it.type == AssignNode
        }

        if (assignIndex == -1) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val assignNode = children[assignIndex]
        if (assignNode !is Node.Leaf) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val prevNode = children.getOrNull(assignIndex - 1)
        val nextNode = children.getOrNull(assignIndex + 1)

        val assignWithoutSpaces = TriviaManipulator.removeTrailing(
            TriviaManipulator.removeLeading(assignNode, SpaceTrivia) as Node.Leaf,
            SpaceTrivia,
        ) as Node.Leaf

        val prevWithoutSpaces = prevNode?.let { TriviaManipulator.removeTrailing(it, SpaceTrivia) }
        val nextWithoutSpaces = nextNode?.let { TriviaManipulator.removeLeading(it, SpaceTrivia) }

        val space = listOf(Trivia(SpaceTrivia, " ", assignNode.span))
        val updatedAssign = TriviaManipulator.addTrailing(
            TriviaManipulator.addLeading(assignWithoutSpaces, space) as Node.Leaf,
            space,
        )

        val updatedChildren = children.toMutableList().apply {
            if (prevWithoutSpaces != null) set(assignIndex - 1, prevWithoutSpaces)
            set(assignIndex, updatedAssign)
            if (nextWithoutSpaces != null) set(assignIndex + 1, nextWithoutSpaces)
        }

        val updatedNode = node.copy(children = updatedChildren)
        return VisitResult(Outcome.Ok(NodeValue(updatedNode)), context)
    }
}

package formatter.visitor

import common.model.node.ColonNode
import common.model.node.ConstDeclarationStatementNode
import common.model.node.LetDeclarationStatementNode
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

internal class SpacingAfterColonVisitor(
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
        if (!enforce || !isDeclarationStatement(node)) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val children = node.children.toList()
        val colonIndex = children.indexOfFirst {
            it is Node.Leaf && it.type == ColonNode
        }

        if (colonIndex == -1) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val colonNode = children[colonIndex]
        if (colonNode !is Node.Leaf) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val nextNode = children.getOrNull(colonIndex + 1)
            ?: return VisitResult(Outcome.Ok(NoneValue), context)

        val colonWithoutSpaces = TriviaManipulator.removeTrailing(colonNode, SpaceTrivia) as Node.Leaf
        val nextWithoutSpaces = TriviaManipulator.removeLeading(nextNode, SpaceTrivia)

        val space = listOf(Trivia(SpaceTrivia, " ", colonNode.span))
        val updatedColon = TriviaManipulator.addTrailing(colonWithoutSpaces, space)

        val updatedChildren = children.toMutableList().apply {
            set(colonIndex, updatedColon)
            set(colonIndex + 1, nextWithoutSpaces)
        }

        val updatedNode = node.copy(children = updatedChildren)
        return VisitResult(Outcome.Ok(NodeValue(updatedNode)), context)
    }

    private fun isDeclarationStatement(node: Node.Composite): Boolean {
        return node.type == LetDeclarationStatementNode ||
            node.type == ConstDeclarationStatementNode
    }
}

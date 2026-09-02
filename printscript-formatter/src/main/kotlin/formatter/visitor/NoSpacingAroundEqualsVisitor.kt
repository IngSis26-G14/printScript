package formatter.visitor

import common.model.node.AssignNode
import common.model.node.AssignStatementNode
import common.model.node.ConstDeclarationStatementNode
import common.model.node.LetDeclarationStatementNode
import common.model.node.Node
import common.model.trivia.SpaceTrivia
import common.model.value.NoneValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import formatter.manipulator.TriviaManipulator
import formatter.model.value.NodeValue

internal class NoSpacingAroundEqualsVisitor(
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
        if (!enforce || !isRelevantNode(node)) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val children = node.children.toList()
        val assignIndex = children.indexOfFirst {
            it is Node.Leaf && it.type == AssignNode
        }

        if (assignIndex == -1) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val updatedChildren = children.mapIndexed { i, child ->
            when {
                i == assignIndex -> {
                    val withoutTrailing = TriviaManipulator.removeTrailing(child, SpaceTrivia)
                    TriviaManipulator.removeLeading(withoutTrailing, SpaceTrivia)
                }
                i == assignIndex - 1 -> {
                    TriviaManipulator.removeTrailing(child, SpaceTrivia)
                }
                i == assignIndex + 1 -> {
                    TriviaManipulator.removeLeading(child, SpaceTrivia)
                }
                else -> child
            }
        }

        val updatedNode = node.copy(children = updatedChildren)
        return VisitResult(Outcome.Ok(NodeValue(updatedNode)), context)
    }

    private fun isRelevantNode(node: Node.Composite): Boolean {
        return node.type == LetDeclarationStatementNode ||
            node.type == ConstDeclarationStatementNode ||
            node.type == AssignStatementNode
    }
}

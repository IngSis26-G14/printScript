package formatter.visitor

import common.model.node.BlockNode
import common.model.node.ElseBlockNode
import common.model.node.IfStatementNode
import common.model.node.LeftBraceNode
import common.model.node.Node
import common.model.trivia.NewlineTrivia
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

internal class IfBraceBelowLineVisitor(private val enforce: Boolean) : ContextVisitor {
    override fun visit(node: Node.Leaf, table: ContextVisitorTable, context: VisitorContext): VisitResult =
        VisitResult(Outcome.Ok(NoneValue), context)

    override fun visit(node: Node.Composite, table: ContextVisitorTable, context: VisitorContext): VisitResult {
        if (!enforce || node.type != IfStatementNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }
        return VisitResult(
            Outcome.Ok(NodeValue(node.copy(children = node.children.map(::placeBraceBelowLine)))),
            context,
        )
    }

    private fun placeBraceBelowLine(node: Node): Node = when (node) {
        is Node.Leaf -> if (node.type == LeftBraceNode) normalizeBrace(node) else node
        is Node.Composite -> if (node.type in setOf(BlockNode, ElseBlockNode)) {
            node.copy(children = node.children.map(::placeBraceBelowLine))
        } else {
            node
        }
    }

    private fun normalizeBrace(node: Node): Node {
        val cleaned = TriviaManipulator.removeLeading(
            TriviaManipulator.removeLeading(node, NewlineTrivia),
            SpaceTrivia,
        )
        return TriviaManipulator.addLeading(cleaned, listOf(Trivia(NewlineTrivia, "\n", node.span)))
    }
}

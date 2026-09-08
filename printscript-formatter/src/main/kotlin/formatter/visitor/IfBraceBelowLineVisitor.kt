package formatter.visitor

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
import formatter.transformer.NodeTransformer

internal class IfBraceBelowLineVisitor(
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
        if (!enforce || node.type != IfStatementNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val transformedChildren = node.children.map { child ->
            processChild(child)
        }

        val updatedNode = node.copy(children = transformedChildren)
        val transformed = NodeTransformer.transformRecursive(updatedNode, table, context)

        return VisitResult(Outcome.Ok(NodeValue(transformed)), context)
    }

    private fun processChild(node: Node): Node {
        return when (node) {
            is Node.Leaf -> {
                if (node.type == LeftBraceNode) {
                    val withoutSpaces = TriviaManipulator.removeLeading(node, SpaceTrivia)
                    val withoutNewlines = TriviaManipulator.removeLeading(
                        withoutSpaces,
                        NewlineTrivia,
                    )

                    val newline = listOf(Trivia(NewlineTrivia, "\n", node.span))
                    TriviaManipulator.addLeading(withoutNewlines, newline)
                } else {
                    node
                }
            }
            is Node.Composite -> {
                val updatedChildren = node.children.map { child -> processChild(child) }
                node.copy(children = updatedChildren)
            }
        }
    }
}

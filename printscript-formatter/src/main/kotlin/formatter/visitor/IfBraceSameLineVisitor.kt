package formatter.visitor

import common.model.node.BlockNode
import common.model.node.ElseBlockNode
import common.model.node.IfStatementNode
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

internal class IfBraceSameLineVisitor(private val enforce: Boolean) : ContextVisitor {
    override fun visit(node: Node.Leaf, table: ContextVisitorTable, context: VisitorContext): VisitResult =
        VisitResult(Outcome.Ok(NoneValue), context)

    override fun visit(node: Node.Composite, table: ContextVisitorTable, context: VisitorContext): VisitResult {
        if (!enforce || node.type !in setOf(IfStatementNode, ElseBlockNode)) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }
        val updated = node.children.map { child ->
            if (child.type == BlockNode || child.type == ElseBlockNode) {
                val cleaned = TriviaManipulator.removeLeading(
                    TriviaManipulator.removeLeading(child, NewlineTrivia),
                    SpaceTrivia,
                )
                TriviaManipulator.addLeading(cleaned, listOf(Trivia(SpaceTrivia, " ", child.span)))
            } else {
                child
            }
        }
        return VisitResult(Outcome.Ok(NodeValue(node.copy(children = updated))), context)
    }
}

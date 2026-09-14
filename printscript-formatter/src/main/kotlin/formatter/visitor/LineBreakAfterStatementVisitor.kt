package formatter.visitor

import common.model.node.IfStatementNode
import common.model.node.LeftBraceNode
import common.model.node.Node
import common.model.node.SemicolonNode
import common.model.trivia.NewlineTrivia
import common.model.trivia.Trivia
import common.model.value.NoneValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import formatter.manipulator.TriviaManipulator
import formatter.model.value.NodeValue

internal class LineBreakAfterStatementVisitor(private val enforce: Boolean) : ContextVisitor {
    override fun visit(node: Node.Leaf, table: ContextVisitorTable, context: VisitorContext): VisitResult =
        apply(node, node.type == SemicolonNode || node.type == LeftBraceNode, context)

    override fun visit(node: Node.Composite, table: ContextVisitorTable, context: VisitorContext): VisitResult =
        apply(node, node.type == IfStatementNode, context)

    private fun apply(node: Node, matches: Boolean, context: VisitorContext): VisitResult {
        if (!enforce || !matches) return VisitResult(Outcome.Ok(NoneValue), context)
        val cleaned = TriviaManipulator.removeTrailing(node, NewlineTrivia)
        val updated = TriviaManipulator.addTrailing(cleaned, listOf(Trivia(NewlineTrivia, "\n", node.span)))
        return VisitResult(Outcome.Ok(NodeValue(updated)), context)
    }
}

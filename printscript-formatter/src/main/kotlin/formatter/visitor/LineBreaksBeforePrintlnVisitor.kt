package formatter.visitor

import common.model.node.Node
import common.model.node.PrintlnStatementNode
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

internal class LineBreaksBeforePrintlnVisitor(private val lineBreaks: Int) : ContextVisitor {
    override fun visit(node: Node.Leaf, table: ContextVisitorTable, context: VisitorContext): VisitResult =
        VisitResult(Outcome.Ok(NoneValue), context)

    override fun visit(node: Node.Composite, table: ContextVisitorTable, context: VisitorContext): VisitResult {
        if (node.type != PrintlnStatementNode) return VisitResult(Outcome.Ok(NoneValue), context)
        val cleaned = TriviaManipulator.removeLeading(node, NewlineTrivia)
        val newlines = List(lineBreaks) { Trivia(NewlineTrivia, "\n", node.span) }
        val updated = TriviaManipulator.addLeading(cleaned, newlines)
        return VisitResult(Outcome.Ok(NodeValue(updated)), context)
    }
}

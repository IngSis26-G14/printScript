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

internal class MandatorySingleSpaceVisitor(private val enforce: Boolean) : ContextVisitor {
    override fun visit(node: Node.Leaf, table: ContextVisitorTable, context: VisitorContext): VisitResult =
        VisitResult(Outcome.Ok(NoneValue), context)

    override fun visit(node: Node.Composite, table: ContextVisitorTable, context: VisitorContext): VisitResult {
        if (!enforce) return VisitResult(Outcome.Ok(NoneValue), context)
        val children = node.children.toList()
        if (children.size < 2) return VisitResult(Outcome.Ok(NoneValue), context)

        val cleaned = children.map { child ->
            TriviaManipulator.removeLeading(
                TriviaManipulator.removeTrailing(child, SpaceTrivia),
                SpaceTrivia,
            )
        }
        val separated = cleaned.mapIndexed { index, current ->
            val next = cleaned.getOrNull(index + 1)
            if (next != null && (next !is Node.Leaf || next.type != SemicolonNode)) {
                TriviaManipulator.addTrailing(
                    current,
                    listOf(Trivia(SpaceTrivia, " ", current.span)),
                )
            } else {
                current
            }
        }
        return VisitResult(Outcome.Ok(NodeValue(node.copy(children = separated))), context)
    }
}

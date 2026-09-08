package formatter.visitor

import common.model.node.Node
import common.model.node.SemicolonNode
import common.model.trivia.NewlineTrivia
import common.model.trivia.Trivia
import common.model.value.NoneValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.option.Option
import common.type.outcome.Outcome
import formatter.manipulator.TriviaManipulator
import formatter.model.context.LineBreakState
import formatter.model.value.NodeValue
import formatter.transformer.NodeTransformer
import formatter.traversal.NodeTraversal

internal class LineBreakAfterStatementVisitor(
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
            val newContext = initializeStateIfNeeded(context)
            return VisitResult(Outcome.Ok(NoneValue), newContext)
        }

        val state = getOrInitializeState(context)

        val (nodeWithoutOriginalLeading, originalLeadingNewlines) =
            TriviaManipulator.extractLeading(node, NewlineTrivia)

        val nodeWithLeading = if (state.isFirstNode) {
            nodeWithoutOriginalLeading
        } else {
            val newlinesToAdd = if (originalLeadingNewlines.isEmpty()) {
                state.pendingNewlines
            } else {
                originalLeadingNewlines
            }
            TriviaManipulator.addLeading(nodeWithoutOriginalLeading, newlinesToAdd)
        }

        val transformed = NodeTransformer.transformRecursive(nodeWithLeading, table, context)

        val (nodeWithoutTrailing, extractedNewlines) =
            TriviaManipulator.extractTrailing(transformed, NewlineTrivia)

        val newlinesToTransfer = calculateNewlinesToTransfer(
            nodeWithoutTrailing,
            extractedNewlines,
        )

        val newState = LineBreakState(pendingNewlines = newlinesToTransfer, isFirstNode = false)
        val newContext = context.register(LineBreakState::class, newState)

        return VisitResult(Outcome.Ok(NodeValue(nodeWithoutTrailing)), newContext)
    }

    private fun initializeStateIfNeeded(context: VisitorContext): VisitorContext {
        return if (context.get(LineBreakState::class) is Option.None) {
            context.register(LineBreakState::class, LineBreakState())
        } else {
            context
        }
    }

    private fun getOrInitializeState(context: VisitorContext): LineBreakState {
        return when (val opt = context.get(LineBreakState::class)) {
            is Option.Some -> opt.value
            is Option.None -> LineBreakState()
        }
    }

    private fun calculateNewlinesToTransfer(
        node: Node,
        extractedNewlines: List<Trivia>,
    ): List<Trivia> {
        val endsWithSemicolon = NodeTraversal.endsWith(node, SemicolonNode)

        return if (endsWithSemicolon) {
            if (extractedNewlines.isEmpty()) {
                listOf(Trivia(NewlineTrivia, "\n", node.span))
            } else {
                extractedNewlines
            }
        } else {
            extractedNewlines
        }
    }
}

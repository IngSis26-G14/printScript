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
import common.type.option.Option
import common.type.outcome.Outcome
import formatter.manipulator.TriviaManipulator
import formatter.model.context.PrintlnState
import formatter.model.value.NodeValue
import formatter.transformer.NodeTransformer

internal class LineBreaksAfterPrintlnVisitor(
    private val lineBreaks: Int,
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
        val state = getOrInitializeState(context)
        val isPrintln = node.type == PrintlnStatementNode

        val nodeWithoutLeadingNewlines = if (state.pendingNewlines.isNotEmpty()) {
            TriviaManipulator.removeLeading(node, NewlineTrivia)
        } else {
            node
        }
        val nodeWithLeading = TriviaManipulator.addLeading(
            nodeWithoutLeadingNewlines,
            state.pendingNewlines,
        )

        val transformed = NodeTransformer.transformRecursive(nodeWithLeading, table, context)

        val (nodeWithoutTrailing, extractedNewlines) =
            TriviaManipulator.extractTrailing(transformed, NewlineTrivia)

        val newlinesToTransfer = if (isPrintln) {
            calculateNewlinesAfterPrintln(nodeWithoutTrailing, extractedNewlines)
        } else {
            extractedNewlines
        }

        val newState = state.copy(
            pendingNewlines = newlinesToTransfer,
            hadPreviousPrintln = isPrintln,
        )
        val newContext = context.register(PrintlnState::class, newState)

        return VisitResult(Outcome.Ok(NodeValue(nodeWithoutTrailing)), newContext)
    }

    private fun getOrInitializeState(context: VisitorContext): PrintlnState {
        return when (val opt = context.get(PrintlnState::class)) {
            is Option.Some -> opt.value
            is Option.None -> PrintlnState()
        }
    }

    private fun calculateNewlinesAfterPrintln(
        node: Node,
        extractedNewlines: List<Trivia>,
    ): List<Trivia> {
        val currentNewlineCount = extractedNewlines.size
        val requiredNewlineCount = lineBreaks + 1

        return when {
            currentNewlineCount > requiredNewlineCount -> {
                extractedNewlines.take(requiredNewlineCount)
            }
            currentNewlineCount < requiredNewlineCount -> {
                val additionalNeeded = requiredNewlineCount - currentNewlineCount
                val additional = List(additionalNeeded) {
                    Trivia(NewlineTrivia, "\n", node.span)
                }
                extractedNewlines + additional
            }
            else -> {
                extractedNewlines
            }
        }
    }
}

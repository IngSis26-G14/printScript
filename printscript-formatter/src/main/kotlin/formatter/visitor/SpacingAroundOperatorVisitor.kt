package formatter.visitor

import common.model.node.BinaryOperationExpressionNode
import common.model.node.Node
import common.model.node.NodeType
import common.model.trivia.SpaceTrivia
import common.model.trivia.Trivia
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import formatter.manipulator.TriviaManipulator
import formatter.model.value.NodeValue
import formatter.transformer.NodeTransformer

internal class SpacingAroundOperatorVisitor(
    private val enforce: Boolean,
    private val operators: Collection<NodeType>,
) : ContextVisitor {

    override fun visit(
        node: Node.Leaf,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult {
        val adjusted = if (node.type in operators) {
            if (enforce) {
                ensureSpacesAround(node)
            } else {
                removeSpacesAround(node)
            }
        } else {
            node
        }
        return VisitResult(Outcome.Ok(NodeValue(adjusted)), context)
    }

    override fun visit(
        node: Node.Composite,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult {
        val transformed = if (
            node.type == BinaryOperationExpressionNode &&
            node.children.size == 3
        ) {
            transformBinaryOperation(node, table, context)
        } else {
            NodeTransformer.transformRecursive(node, table, context)
        }

        return VisitResult(Outcome.Ok(NodeValue(transformed)), context)
    }

    private fun transformBinaryOperation(
        node: Node.Composite,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): Node.Composite {
        val children = node.children.toList()

        val leftDispatched = NodeTransformer.applyDispatchResult(
            children[0],
            table.dispatch(children[0], context),
        )
        val leftOperand = NodeTransformer.transformRecursive(leftDispatched, table, context)

        val operatorDispatched = NodeTransformer.applyDispatchResult(
            children[1],
            table.dispatch(children[1], context),
        )
        val operator = NodeTransformer.transformRecursive(operatorDispatched, table, context)

        val rightDispatched = NodeTransformer.applyDispatchResult(
            children[2],
            table.dispatch(children[2], context),
        )
        val rightOperand = NodeTransformer.transformRecursive(rightDispatched, table, context)

        return node.copy(children = listOf(leftOperand, operator, rightOperand))
    }

    private fun ensureSpacesAround(operator: Node.Leaf): Node.Leaf {
        var result = operator

        val hasLeadingSpace = result.leading.lastOrNull()?.type == SpaceTrivia
        if (!hasLeadingSpace) {
            val space = Trivia(SpaceTrivia, " ", operator.span)
            result = TriviaManipulator.addLeading(result, listOf(space)) as Node.Leaf
        }

        val hasTrailingSpace = result.trailing.firstOrNull()?.type == SpaceTrivia
        if (!hasTrailingSpace) {
            val space = Trivia(SpaceTrivia, " ", operator.span)
            result = TriviaManipulator.addTrailing(result, listOf(space)) as Node.Leaf
        }

        return result
    }

    private fun removeSpacesAround(operator: Node.Leaf): Node.Leaf {
        var result = operator
        result = TriviaManipulator.removeLeading(result, SpaceTrivia) as Node.Leaf
        result = TriviaManipulator.removeTrailing(result, SpaceTrivia) as Node.Leaf
        return result
    }
}

package validator.visitor

import common.model.node.BlockNode
import common.model.node.ElseBlockNode
import common.model.node.ElseNode
import common.model.node.IfStatementNode
import common.model.node.LeftParenthesisNode
import common.model.node.Node
import common.model.node.RightParenthesisNode
import common.model.value.NoneValue
import common.model.value.type.BooleanValueType
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome
import common.type.outcome.getOrElse
import validator.model.category.TypeMismatch
import validator.model.error.ValidationError
import validator.model.value.RuntimeValueType

internal class IfStatementVisitor : ContextVisitor {

    override fun visit(
        node: Node.Leaf,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult = VisitResult(Outcome.Ok(NoneValue), context)

    override fun visit(
        node: Node.Composite,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult {
        if (node.type != IfStatementNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val children = node.children.toList()
        val arguments = children
            .dropWhile { it.type != LeftParenthesisNode }
            .drop(1)
            .takeWhile { it.type != RightParenthesisNode }

        if (arguments.isEmpty()) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val conditionNode = arguments[0]
        val conditionVisit = table.dispatch(conditionNode, context)
        if (conditionVisit.outcome is Outcome.Error) return conditionVisit

        val condition = conditionVisit.outcome.getOrElse { return conditionVisit }

        if (condition.type !is BooleanValueType && condition.type !is RuntimeValueType) {
            val message = "If condition must be a boolean, got ${condition.type.name}"
            val error = ValidationError(message, TypeMismatch, conditionNode.span)
            return VisitResult(Outcome.Error(error), conditionVisit.context)
        }

        val thenBlocks = children
            .dropWhile { it.type != RightParenthesisNode }
            .drop(1)
            .filter { it.type == BlockNode }

        if (thenBlocks.isEmpty()) {
            return VisitResult(Outcome.Ok(NoneValue), conditionVisit.context)
        }

        val thenBlock = thenBlocks[0]
        val thenVisit = table.dispatch(thenBlock, conditionVisit.context)
        if (thenVisit.outcome is Outcome.Error) return thenVisit

        val elseBlocks = children.filter { it.type == ElseBlockNode }
        if (elseBlocks.isEmpty()) {
            return VisitResult(Outcome.Ok(NoneValue), thenVisit.context)
        }

        val elseChildren = when (val elseBlock = elseBlocks[0]) {
            is Node.Composite -> elseBlock.children.toList()
            is Node.Leaf -> return VisitResult(Outcome.Ok(NoneValue), thenVisit.context)
        }

        val elseBody = elseChildren
            .dropWhile { it.type != ElseNode }
            .drop(1)

        if (elseBody.isEmpty()) {
            return VisitResult(Outcome.Ok(NoneValue), thenVisit.context)
        }

        return table.dispatch(elseBody[0], thenVisit.context)
    }
}

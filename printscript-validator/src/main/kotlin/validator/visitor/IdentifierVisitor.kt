package validator.visitor

import common.model.node.IdentifierNode
import common.model.node.Node
import common.model.value.NoneValue
import common.model.value.StringValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.option.getOrElse
import common.type.outcome.Outcome
import validator.model.category.UndefinedIdentifier
import validator.model.error.SystemError
import validator.model.error.ValidationError
import validator.model.value.RuntimeValue
import validator.util.StaticSymbolTable

internal class IdentifierVisitor : ContextVisitor {

    override fun visit(
        node: Node.Leaf,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult {
        if (node.type != IdentifierNode) {
            return VisitResult(Outcome.Ok(node.value), context)
        }

        val symbolTable = context.get(StaticSymbolTable::class).getOrElse {
            val error = SystemError("Internal validation error")
            return VisitResult(Outcome.Error(error), context)
        }

        val identifierName = when (val value = node.value) {
            is StringValue -> value.value
            else -> node.value.format()
        }

        val symbolInfo = symbolTable.get(identifierName).getOrElse {
            val message = "Identifier '$identifierName' not declared in this scope"
            val error = ValidationError(
                message,
                UndefinedIdentifier,
                node.span,
            )
            return VisitResult(Outcome.Error(error), context)
        }

        val resultValue = if (symbolInfo.value is RuntimeValue) {
            RuntimeValue(symbolInfo.declaredType)
        } else {
            symbolInfo.value
        }

        return VisitResult(Outcome.Ok(resultValue), context)
    }

    override fun visit(
        node: Node.Composite,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult = VisitResult(Outcome.Ok(NoneValue), context)
}

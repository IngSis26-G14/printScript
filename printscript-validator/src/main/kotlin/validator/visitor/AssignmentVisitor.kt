package validator.visitor

import common.model.node.AssignNode
import common.model.node.AssignStatementNode
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
import common.type.outcome.getOrElse
import validator.model.category.InvalidReassignment
import validator.model.category.TypeMismatch
import validator.model.category.UndefinedIdentifier
import validator.model.error.SystemError
import validator.model.error.ValidationError
import validator.model.symbol.StaticSymbol
import validator.model.value.RuntimeValue
import validator.model.value.RuntimeValueType
import validator.util.StaticSymbolTable

internal class AssignmentVisitor : ContextVisitor {

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
        if (node.type != AssignStatementNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val symbolTable = context.get(StaticSymbolTable::class).getOrElse {
            val message = "Internal validation error"
            val error = SystemError(message)
            return VisitResult(Outcome.Error(error), context)
        }

        val children = node.children.toList()

        val identifierNodes = children.filter { it.type == IdentifierNode }
        if (identifierNodes.isEmpty()) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val identifierNode = identifierNodes[0]
        val identifierName = when (identifierNode) {
            is Node.Leaf -> {
                when (val value = identifierNode.value) {
                    is StringValue -> value.value
                    else -> identifierNode.value.format()
                }
            }
            is Node.Composite -> return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val symbolInfo = symbolTable.get(identifierName).getOrElse {
            val message = "Identifier '$identifierName' not declared in this scope"
            val error = ValidationError(
                message,
                UndefinedIdentifier,
                identifierNode.span,
            )
            return VisitResult(Outcome.Error(error), context)
        }

        if (!symbolInfo.isMutable) {
            val message = "Cannot reassign const identifier '$identifierName'"
            val error = ValidationError(message, InvalidReassignment, identifierNode.span)
            return VisitResult(Outcome.Error(error), context)
        }

        val assignNodes = children.filter { it.type == AssignNode }
        if (assignNodes.isEmpty()) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val assignNode = assignNodes[0]
        val assignIndex = children.indexOf(assignNode)
        if (assignIndex + 1 >= children.size) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val valueNode = children[assignIndex + 1]
        val valueVisit = table.dispatch(valueNode, context)

        if (valueVisit.outcome is Outcome.Error) {
            val fallback = RuntimeValue(symbolInfo.declaredType)
            val updatedSymbol = StaticSymbol(
                identifierName,
                fallback,
                symbolInfo.declaredType,
                symbolInfo.declaredAt,
                symbolInfo.isMutable,
            )
            val updatedTable = symbolTable.set(identifierName, updatedSymbol)
            val updatedContext = context.register(StaticSymbolTable::class, updatedTable)
            return VisitResult(valueVisit.outcome, updatedContext)
        }

        val assignedValue = valueVisit.outcome.getOrElse { return valueVisit }

        val actualType = if (assignedValue is RuntimeValue) {
            assignedValue.type
        } else {
            assignedValue.type
        }

        if (actualType != RuntimeValueType && symbolInfo.declaredType != actualType) {
            val message =
                "Cannot assign ${actualType.name} value to identifier of type ${symbolInfo.declaredType.name}"
            val error = ValidationError(message, TypeMismatch, node.span)

            val fallback = RuntimeValue(symbolInfo.declaredType)
            val updatedSymbol = StaticSymbol(
                identifierName,
                fallback,
                symbolInfo.declaredType,
                symbolInfo.declaredAt,
                symbolInfo.isMutable,
            )
            val updatedTable = symbolTable.set(identifierName, updatedSymbol)
            val updatedContext = context.register(StaticSymbolTable::class, updatedTable)
            return VisitResult(Outcome.Error(error), updatedContext)
        }

        val updatedSymbol = StaticSymbol(
            identifierName,
            assignedValue,
            symbolInfo.declaredType,
            symbolInfo.declaredAt,
            symbolInfo.isMutable,
        )

        val newTable = symbolTable.set(identifierName, updatedSymbol)
        val newContext = context.register(StaticSymbolTable::class, newTable)
        return VisitResult(Outcome.Ok(NoneValue), newContext)
    }
}

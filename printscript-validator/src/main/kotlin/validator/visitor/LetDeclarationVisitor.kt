package validator.visitor

import common.model.node.AssignNode
import common.model.node.IdentifierNode
import common.model.node.LetDeclarationStatementNode
import common.model.node.Node
import common.model.node.NodeType
import common.model.value.NoneValue
import common.model.value.type.ValueType
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.option.getOrElse
import common.type.outcome.Outcome
import common.type.outcome.getOrElse
import validator.model.category.InvalidType
import validator.model.category.Redeclaration
import validator.model.category.TypeMismatch
import validator.model.error.SystemError
import validator.model.error.ValidationError
import validator.model.symbol.StaticSymbol
import validator.model.value.RuntimeValue
import validator.model.value.RuntimeValueType
import validator.util.StaticSymbolTable

internal class LetDeclarationVisitor(
    private val typeMap: Map<NodeType, ValueType>,
) : ContextVisitor {

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
        if (node.type != LetDeclarationStatementNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val symbolTable = context.get(StaticSymbolTable::class).getOrElse {
            val error = SystemError("Internal validation error")
            return VisitResult(Outcome.Error(error), context)
        }

        val children = node.children.toList()
        val identifierNode = children.first { it.type == IdentifierNode } as Node.Leaf
        val identifier = identifierNode.value.format()

        if (symbolTable.contains(identifier)) {
            val message = "Identifier '$identifier' already declared in this scope"
            val error = ValidationError(
                message,
                Redeclaration,
                identifierNode.span,
            )
            return VisitResult(Outcome.Error(error), context)
        }

        val typeNode = (children.first { it.type in typeMap.keys } as Node.Leaf)
        val declaredType = typeMap[typeNode.type] ?: run {
            val error = ValidationError(
                "Invalid type '${typeNode.value.format()}'",
                InvalidType,
                typeNode.span,
            )
            return VisitResult(Outcome.Error(error), context)
        }

        val assignNodes = children.filter { it.type == AssignNode }

        if (assignNodes.isEmpty()) {
            val symbol = StaticSymbol(
                identifier,
                NoneValue,
                declaredType,
                node.span,
                true,
            )
            val newTable = symbolTable.set(identifier, symbol)
            val newContext = context.register(StaticSymbolTable::class, newTable)
            return VisitResult(Outcome.Ok(NoneValue), newContext)
        }

        val assignNode = assignNodes[0]
        val assignIndex = children.indexOf(assignNode)
        val exprNode = children[assignIndex + 1]

        val exprVisit = table.dispatch(exprNode, context)

        if (exprVisit.outcome is Outcome.Error) {
            val symbol = StaticSymbol(
                identifier,
                RuntimeValue(declaredType),
                declaredType,
                node.span,
                true,
            )
            val newTable = symbolTable.set(identifier, symbol)
            val newContext = context.register(StaticSymbolTable::class, newTable)

            return VisitResult(exprVisit.outcome, newContext)
        }

        val exprValue = exprVisit.outcome.getOrElse { return exprVisit }

        val actualType = if (exprValue is RuntimeValue) {
            exprValue.type
        } else {
            exprValue.type
        }

        if (actualType != RuntimeValueType && declaredType != actualType) {
            val message = "Expected ${declaredType.name}, got ${actualType.name}"
            val error = ValidationError(message, TypeMismatch, exprNode.span)

            val symbol = StaticSymbol(
                identifier,
                exprValue,
                declaredType,
                node.span,
                true,
            )
            val newTable = symbolTable.set(identifier, symbol)
            val newContext = context.register(StaticSymbolTable::class, newTable)

            return VisitResult(Outcome.Error(error), newContext)
        }

        val symbol = StaticSymbol(
            identifier,
            exprValue,
            declaredType,
            node.span,
            true,
        )

        val newTable = symbolTable.set(identifier, symbol)
        val newContext = context.register(StaticSymbolTable::class, newTable)
        return VisitResult(Outcome.Ok(NoneValue), newContext)
    }
}

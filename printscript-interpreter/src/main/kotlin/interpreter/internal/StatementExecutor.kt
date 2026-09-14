package interpreter.internal

import common.io.writer.OutputWriter
import common.model.diagnostic.Diagnostic
import common.model.node.AssignNode
import common.model.node.AssignStatementNode
import common.model.node.BlockNode
import common.model.node.BooleanTypeNode
import common.model.node.ConstDeclarationStatementNode
import common.model.node.ElseBlockNode
import common.model.node.IdentifierNode
import common.model.node.IfStatementNode
import common.model.node.LeftBraceNode
import common.model.node.LetDeclarationStatementNode
import common.model.node.Node
import common.model.node.NumberTypeNode
import common.model.node.PrintlnStatementNode
import common.model.node.RightBraceNode
import common.model.node.StringTypeNode
import common.model.value.BooleanValue
import common.model.value.StringValue
import common.model.value.type.BooleanValueType
import common.model.value.type.NumberValueType
import common.model.value.type.StringValueType
import interpreter.internal.diagnostic.InterpreterDiagnostic

internal class StatementExecutor(
    private val symbols: SymbolTable,
    private val evaluator: ExpressionEvaluator,
    private val output: OutputWriter,
) {
    fun execute(node: Node): Diagnostic? = when (node.type) {
        LetDeclarationStatementNode, ConstDeclarationStatementNode -> declare(node)
        IfStatementNode -> conditional(node)
        BlockNode -> block(node)
        AssignStatementNode -> assign(node)
        PrintlnStatementNode -> print(node)
        else -> InterpreterDiagnostic("Unsupported statement '${node.type}'", span = node.span)
    }

    private fun declare(node: Node): Diagnostic? {
        val children = children(node) ?: return malformed(node)
        val identifierIndex = children.indexOfFirst { it.type == IdentifierNode }
        val typeIndex = children.indexOfFirst { it.type in declaredTypes }
        if (identifierIndex < 0 || typeIndex < 0) return malformed(node)
        val name = leafText(children[identifierIndex]) ?: return malformed(node)
        if (symbols.find(name) != null) {
            return InterpreterDiagnostic(
                "Variable '$name' is already declared",
                span = children[identifierIndex].span,
            )
        }

        val declaredType = declaredTypes.getValue(children[typeIndex].type)
        val assignIndex = children.indexOfFirst { it.type == AssignNode }
        val isMutable = node.type != ConstDeclarationStatementNode
        if (!isMutable && assignIndex < 0) return malformed(node)
        val value = if (assignIndex >= 0) {
            val expression = children.getOrNull(assignIndex + 1) ?: return malformed(node)
            when (val result = evaluator.evaluate(expression, declaredType)) {
                is EvaluationResult.Failure -> return result.diagnostic
                is EvaluationResult.Success -> result.value
            }
        } else {
            null
        }
        if (value != null && value.type != declaredType) {
            return InterpreterDiagnostic(
                "Cannot initialize '$name' of type '${declaredType.name}' with '${value.type.name}'",
                span = expressionSpan(children, assignIndex),
            )
        }
        symbols.declare(name, declaredType, value, isMutable)
        return null
    }

    private fun assign(node: Node): Diagnostic? {
        val children = children(node) ?: return malformed(node)
        val name = children.firstOrNull { it.type == IdentifierNode }?.let(::leafText) ?: return malformed(node)
        val identifier = children.firstOrNull { it.type == IdentifierNode } ?: return malformed(node)
        val variable = symbols.find(name) ?: return InterpreterDiagnostic(
            "Variable '$name' is not declared",
            span = identifier.span,
        )
        if (!variable.isMutable) {
            return InterpreterDiagnostic("Cannot reassign constant '$name'", span = identifier.span)
        }
        val assignIndex = children.indexOfFirst { it.type == AssignNode }
        val expression = children.getOrNull(assignIndex + 1) ?: return malformed(node)
        val value = when (val result = evaluator.evaluate(expression, variable.declaredType)) {
            is EvaluationResult.Failure -> return result.diagnostic
            is EvaluationResult.Success -> result.value
        }
        if (value.type != variable.declaredType) {
            return InterpreterDiagnostic(
                "Cannot assign '${value.type.name}' to '$name' of type '${variable.declaredType.name}'",
                span = expression.span,
            )
        }
        symbols.assign(name, value)
        return null
    }

    private fun print(node: Node): Diagnostic? {
        val children = children(node) ?: return malformed(node)
        val expression = children.firstOrNull {
            it.type !in setOf(
                common.model.node.PrintlnKeywordNode,
                common.model.node.LeftParenthesisNode,
                common.model.node.RightParenthesisNode,
                common.model.node.SemicolonNode,
            )
        }
        val text = if (expression == null) {
            ""
        } else {
            when (val result = evaluator.evaluate(expression)) {
                is EvaluationResult.Failure -> return result.diagnostic
                is EvaluationResult.Success -> result.value.format()
            }
        }
        output.write(sequenceOf(text))
        return null
    }

    private fun conditional(node: Node): Diagnostic? {
        val children = children(node) ?: return malformed(node)
        val condition = children.getOrNull(2) ?: return malformed(node)
        val result = evaluator.evaluate(condition, BooleanValueType)
        if (result is EvaluationResult.Failure) return result.diagnostic
        val value = (result as EvaluationResult.Success).value
        if (value !is BooleanValue) {
            return InterpreterDiagnostic("If condition must be a boolean", span = condition.span)
        }
        val thenBlock = children.firstOrNull { it.type == BlockNode } ?: return malformed(node)
        if (value.value) return block(thenBlock)
        val elseBlock = children.firstOrNull { it.type == ElseBlockNode } ?: return null
        val body = children(elseBlock)?.firstOrNull { it.type == BlockNode } ?: return malformed(elseBlock)
        return block(body)
    }

    private fun block(node: Node): Diagnostic? {
        val statements = children(node) ?: return malformed(node)
        for (statement in statements) {
            if (statement.type == LeftBraceNode || statement.type == RightBraceNode) continue
            execute(statement)?.let { return it }
        }
        return null
    }

    private val declaredTypes = mapOf(
        NumberTypeNode to NumberValueType,
        StringTypeNode to StringValueType,
        BooleanTypeNode to BooleanValueType,
    )

    private fun children(node: Node): List<Node>? = (node as? Node.Composite)?.children?.toList()

    private fun leafText(node: Node): String? = ((node as? Node.Leaf)?.value as? StringValue)?.value

    private fun expressionSpan(children: List<Node>, assignIndex: Int) =
        children.getOrNull(assignIndex + 1)?.span

    private fun malformed(node: Node) =
        InterpreterDiagnostic("Malformed '${node.type}' node", span = node.span)
}

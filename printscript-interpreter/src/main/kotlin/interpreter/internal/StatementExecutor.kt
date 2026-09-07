package interpreter.internal

import common.io.reader.env.EnvReader
import common.io.reader.input.InputReader
import common.io.writer.OutputWriter
import common.model.diagnostic.Diagnostic
import common.model.node.AssignNode
import common.model.node.AssignStatementNode
import common.model.node.IdentifierNode
import common.model.node.LetDeclarationStatementNode
import common.model.node.Node
import common.model.node.NumberTypeNode
import common.model.node.PrintlnStatementNode
import common.model.node.StringTypeNode
import common.model.value.StringValue
import common.model.value.type.NumberValueType
import common.model.value.type.StringValueType
import interpreter.internal.diagnostic.InterpreterDiagnostic

internal class StatementExecutor(
    private val symbols: SymbolTable,
    private val evaluator: ExpressionEvaluator,
    @Suppress("unused") private val input: InputReader,
    private val output: OutputWriter,
    @Suppress("unused") private val env: EnvReader,
) {
    fun execute(node: Node): Diagnostic? = when (node.type) {
        LetDeclarationStatementNode -> declare(node)
        AssignStatementNode -> assign(node)
        PrintlnStatementNode -> print(node)
        else -> InterpreterDiagnostic("Unsupported statement '${node.type}'")
    }

    private fun declare(node: Node): Diagnostic? {
        val children = children(node) ?: return malformed(node)
        val identifierIndex = children.indexOfFirst { it.type == IdentifierNode }
        val typeIndex = children.indexOfFirst { it.type == NumberTypeNode || it.type == StringTypeNode }
        if (identifierIndex < 0 || typeIndex < 0) return malformed(node)
        val name = leafText(children[identifierIndex]) ?: return malformed(node)
        if (symbols.find(name) != null) return InterpreterDiagnostic("Variable '$name' is already declared")

        val declaredType = if (children[typeIndex].type == NumberTypeNode) NumberValueType else StringValueType
        val assignIndex = children.indexOfFirst { it.type == AssignNode }
        val value = if (assignIndex >= 0) {
            val expression = children.getOrNull(assignIndex + 1) ?: return malformed(node)
            when (val result = evaluator.evaluate(expression)) {
                is EvaluationResult.Failure -> return result.diagnostic
                is EvaluationResult.Success -> result.value
            }
        } else {
            null
        }
        if (value != null && value.type != declaredType) {
            return InterpreterDiagnostic(
                "Cannot initialize '$name' of type '${declaredType.name}' with '${value.type.name}'",
            )
        }
        symbols.declare(name, declaredType, value)
        return null
    }

    private fun assign(node: Node): Diagnostic? {
        val children = children(node) ?: return malformed(node)
        val name = children.firstOrNull { it.type == IdentifierNode }?.let(::leafText) ?: return malformed(node)
        val variable = symbols.find(name) ?: return InterpreterDiagnostic("Variable '$name' is not declared")
        val assignIndex = children.indexOfFirst { it.type == AssignNode }
        val expression = children.getOrNull(assignIndex + 1) ?: return malformed(node)
        val value = when (val result = evaluator.evaluate(expression)) {
            is EvaluationResult.Failure -> return result.diagnostic
            is EvaluationResult.Success -> result.value
        }
        if (value.type != variable.declaredType) {
            return InterpreterDiagnostic(
                "Cannot assign '${value.type.name}' to '$name' of type '${variable.declaredType.name}'",
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

    private fun children(node: Node): List<Node>? = (node as? Node.Composite)?.children?.toList()

    private fun leafText(node: Node): String? = ((node as? Node.Leaf)?.value as? StringValue)?.value

    private fun malformed(node: Node) = InterpreterDiagnostic("Malformed '${node.type}' node")
}

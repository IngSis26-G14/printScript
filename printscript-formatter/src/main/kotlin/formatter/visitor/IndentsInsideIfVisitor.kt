package formatter.visitor

import common.model.node.BlockNode
import common.model.node.ElseBlockNode
import common.model.node.IfStatementNode
import common.model.node.Node
import common.model.node.RightBraceNode
import common.model.span.Span
import common.model.trivia.NewlineTrivia
import common.model.trivia.SpaceTrivia
import common.model.trivia.Trivia
import common.model.value.NoneValue
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.option.getOrElse
import common.type.outcome.Outcome
import formatter.manipulator.TriviaManipulator
import formatter.model.context.IndentState
import formatter.model.value.NodeValue
import formatter.transformer.NodeTransformer

internal class IndentsInsideIfVisitor(
    private val indents: Int,
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
        if (node.type != IfStatementNode) {
            return VisitResult(Outcome.Ok(NoneValue), context)
        }

        val state = context.get(IndentState::class).getOrElse { IndentState(0) }

        val updatedNode = processIfStatement(node, state.currentLevel)
        val transformed = NodeTransformer.transformRecursive(updatedNode, table, context)

        return VisitResult(Outcome.Ok(NodeValue(transformed)), context)
    }

    private fun processIfStatement(
        node: Node.Composite,
        currentLevel: Int,
    ): Node.Composite {
        val updatedChildren = node.children.map { child ->
            when (child) {
                is Node.Leaf -> child
                is Node.Composite -> {
                    when (child.type) {
                        BlockNode -> processBlock(child, currentLevel)
                        ElseBlockNode -> processElseBlock(child, currentLevel)
                        else -> child
                    }
                }
            }
        }
        return node.copy(children = updatedChildren)
    }

    private fun processBlock(
        block: Node.Composite,
        currentLevel: Int,
    ): Node.Composite {
        val children = block.children.toList()
        if (children.isEmpty()) return block

        val updatedChildren = mutableListOf<Node>()

        for (child in children) {
            when (child) {
                is Node.Leaf -> {
                    if (child.type == RightBraceNode) {
                        val indented = applyIndentationToLeaf(
                            child,
                            currentLevel,
                        )
                        updatedChildren.add(indented)
                    } else {
                        updatedChildren.add(child)
                    }
                }
                is Node.Composite -> {
                    val processedChild = if (child.type == IfStatementNode) {
                        val indented = applyIndentationAfterNewline(
                            child,
                            currentLevel + indents,
                        )
                        processIfStatement(indented, currentLevel + indents)
                    } else {
                        applyIndentationAfterNewline(child, currentLevel + indents)
                    }
                    updatedChildren.add(processedChild)
                }
            }
        }

        return block.copy(children = updatedChildren)
    }

    private fun processElseBlock(
        elseBlock: Node.Composite,
        currentLevel: Int,
    ): Node.Composite {
        val updatedChildren = elseBlock.children.map { child ->
            when (child) {
                is Node.Composite -> {
                    when (child.type) {
                        BlockNode -> processBlock(child, currentLevel)
                        IfStatementNode -> {
                            val indented = applyIndentationAfterNewline(
                                child,
                                currentLevel + indents,
                            )
                            processIfStatement(indented, currentLevel + indents)
                        }
                        else -> child
                    }
                }
                else -> child
            }
        }
        return elseBlock.copy(children = updatedChildren)
    }

    private fun applyIndentationAfterNewline(
        node: Node.Composite,
        totalIndent: Int,
    ): Node.Composite {
        if (totalIndent <= 0) return node
        val (withoutNewlines, newlines) = TriviaManipulator.extractLeading(node, NewlineTrivia)
        val withoutSpaces = TriviaManipulator.removeLeading(withoutNewlines, SpaceTrivia)
        val indentTrivia = createIndentTrivia(totalIndent, node.span)
        val withIndent = TriviaManipulator.addLeading(withoutSpaces, indentTrivia)
        val result = TriviaManipulator.addLeading(withIndent, newlines)
        return result as Node.Composite
    }

    private fun createIndentTrivia(
        spaces: Int,
        span: Span,
    ): List<Trivia> {
        if (spaces <= 0) return emptyList()
        val spaceString = " ".repeat(spaces)
        return listOf(Trivia(SpaceTrivia, spaceString, span))
    }

    private fun applyIndentationToLeaf(
        leaf: Node.Leaf,
        totalIndent: Int,
    ): Node.Leaf {
        if (totalIndent <= 0) return leaf
        val extracted = leaf.leading.filter { it.type == NewlineTrivia }
        val withoutNewlines = leaf.leading.filter { it.type != NewlineTrivia }
        val withoutSpaces = withoutNewlines.filter { it.type != SpaceTrivia }
        val indentTrivia = createIndentTrivia(totalIndent, leaf.span)
        val newLeading = extracted + indentTrivia + withoutSpaces
        return leaf.copy(leading = newLeading)
    }
}

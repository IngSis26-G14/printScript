package common.model.node

import common.model.diagnostic.Diagnostic
import common.model.span.Span
import common.model.trivia.Trivia
import common.model.value.Value
import common.model.visitor.Visitor
import common.model.visitor.VisitorTable
import common.model.visitor.context.ContextVisitor
import common.model.visitor.context.ContextVisitorTable
import common.model.visitor.context.VisitResult
import common.model.visitor.context.VisitorContext
import common.type.outcome.Outcome

sealed class Node {
    abstract val type: NodeType
    abstract val span: Span
    abstract fun accept(
        visitor: Visitor,
        table: VisitorTable,
    ): Outcome<Value, Diagnostic>
    abstract fun accept(
        visitor: ContextVisitor,
        table: ContextVisitorTable,
        context: VisitorContext,
    ): VisitResult
    abstract fun format(): String

    data class Leaf(
        override val type: NodeType,
        val value: Value,
        override val span: Span,
        val leading: Collection<Trivia> = emptyList(),
        val trailing: Collection<Trivia> = emptyList(),
    ) : Node() {
        override fun accept(
            visitor: Visitor,
            table: VisitorTable,
        ) = visitor.visit(this, table)

        override fun accept(
            visitor: ContextVisitor,
            table: ContextVisitorTable,
            context: VisitorContext,
        ) = visitor.visit(this, table, context)

        override fun format(): String {
            return buildString {
                leading.forEach { append(it.lexeme) }
                append(value.format())
                trailing.forEach { append(it.lexeme) }
            }
        }
    }

    data class Composite(
        override val type: NodeType,
        val children: Collection<Node>,
        override val span: Span,
    ) : Node() {
        override fun accept(
            visitor: Visitor,
            table: VisitorTable,
        ) = visitor.visit(this, table)

        override fun accept(
            visitor: ContextVisitor,
            table: ContextVisitorTable,
            context: VisitorContext,
        ) = visitor.visit(this, table, context)

        override fun format(): String {
            return children.joinToString("") { it.format() }
        }
    }
}

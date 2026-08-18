package parser.internal.model.grammar.statement

import common.model.node.AssignNode
import common.model.node.AssignStatementNode
import common.model.node.IdentifierNode
import common.model.node.Node
import common.model.node.SemicolonNode
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType
import common.model.value.StringValue
import common.type.outcome.Outcome
import common.type.outcome.getOrElse
import parser.internal.model.category.MissingAssignment
import parser.internal.model.category.MissingEndOfLine
import parser.internal.model.category.MissingIdentifier
import parser.internal.model.grammar.GrammarFail
import parser.internal.model.grammar.GrammarMatch
import parser.internal.table.GrammarTable

internal class AssignStatement : Statement {
    override val type = AssignStatementNode

    override fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail> {
        var consumed = 0

        val identifier = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected identifier at start of assignment",
                    MissingIdentifier,
                    consumed,
                ),
            )
        }
        if (identifier.type != TokenType.IDENTIFIER) {
            return Outcome.Error(
                GrammarFail(
                    "Expected identifier, got '${identifier.lexeme}'",
                    MissingIdentifier,
                    consumed,
                ),
            )
        }

        val equals = tokens.getOrElse(consumed + 1) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '=' after identifier",
                    MissingAssignment,
                    consumed,
                ),
            )
        }
        if (equals.type != TokenType.ASSIGNMENT_OPERATOR) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '=', got '${equals.lexeme}'",
                    MissingAssignment,
                    consumed,
                ),
            )
        }

        consumed += 2

        val rhsResult = table.dispatchExpression(tokens.drop(consumed))
        val rhs = rhsResult.getOrElse {
            return Outcome.Error(
                GrammarFail(
                    it.message,
                    it.category,
                    consumed + it.consumed,
                ),
            )
        }
        consumed += rhs.consumed

        val semicolon = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected ';' after expression",
                    MissingEndOfLine,
                    consumed,
                ),
            )
        }
        if (semicolon.type != TokenType.SEMICOLON) {
            return Outcome.Error(
                GrammarFail(
                    "Expected ';', got '${semicolon.lexeme}'",
                    MissingEndOfLine,
                    consumed,
                ),
            )
        }
        consumed += 1

        val node = buildNode(identifier, equals, rhs.node, semicolon, tokens)
        return Outcome.Ok(GrammarMatch(node, consumed))
    }

    private fun buildNode(
        identifier: Token,
        equals: Token,
        rhs: Node,
        semicolon: Token,
        tokens: List<Token>,
    ): Node {
        val children = listOf(
            Node.Leaf(
                IdentifierNode,
                StringValue(identifier.lexeme),
                identifier.span,
                identifier.leading,
                identifier.trailing,
            ),
            Node.Leaf(
                AssignNode,
                StringValue(equals.lexeme),
                equals.span,
                equals.leading,
                equals.trailing,
            ),
            rhs,
            Node.Leaf(
                SemicolonNode,
                StringValue(semicolon.lexeme),
                semicolon.span,
                semicolon.leading,
                semicolon.trailing,
            ),
        )

        return Node.Composite(
            children = children,
            type = type,
            span = Span(tokens.first().span.start, semicolon.span.end),
        )
    }
}

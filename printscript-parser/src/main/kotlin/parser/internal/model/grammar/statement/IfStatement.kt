package parser.internal.model.grammar.statement

import common.model.node.ElseBlockNode
import common.model.node.ElseNode
import common.model.node.IfNode
import common.model.node.IfStatementNode
import common.model.node.LeftParenthesisNode
import common.model.node.Node
import common.model.node.NodeType
import common.model.node.RightParenthesisNode
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType
import common.model.value.StringValue
import common.type.option.Option
import common.type.outcome.Outcome
import common.type.outcome.getOrElse
import parser.internal.model.category.MissingClosingParenthesis
import parser.internal.model.category.MissingIfDeclaration
import parser.internal.model.category.MissingOpeningParenthesis
import parser.internal.model.grammar.GrammarFail
import parser.internal.model.grammar.GrammarMatch
import parser.internal.table.GrammarTable

internal class IfStatement : Statement {
    override val type: NodeType = IfStatementNode

    override fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail> {
        var consumed = 0

        val ifToken = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected 'if'",
                    MissingIfDeclaration,
                    consumed,
                ),
            )
        }
        if (ifToken.type != TokenType.IF) {
            return Outcome.Error(
                GrammarFail(
                    "Expected 'if', got '${ifToken.lexeme}'",
                    MissingIfDeclaration,
                    consumed,
                ),
            )
        }
        consumed += 1

        val lparen = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '('",
                    MissingOpeningParenthesis,
                    consumed,
                ),
            )
        }
        if (lparen.type != TokenType.LEFT_PARENTHESIS) {
            return Outcome.Error(
                GrammarFail(
                    "Expected '(', got '${lparen.lexeme}'",
                    MissingOpeningParenthesis,
                    consumed,
                ),
            )
        }
        consumed += 1

        val condition = table.dispatchExpression(tokens.drop(consumed)).getOrElse {
            return Outcome.Error(
                GrammarFail(
                    it.message,
                    it.category,
                    consumed + it.consumed,
                ),
            )
        }
        consumed += condition.consumed

        val rparen = tokens.getOrElse(consumed) {
            return Outcome.Error(
                GrammarFail(
                    "Expected ')'",
                    MissingClosingParenthesis,
                    consumed,
                ),
            )
        }
        if (rparen.type != TokenType.RIGHT_PARENTHESIS) {
            return Outcome.Error(
                GrammarFail(
                    "Expected ')', got '${rparen.lexeme}'",
                    MissingClosingParenthesis,
                    consumed,
                ),
            )
        }
        consumed += 1

        val thenBlock = table.dispatchExpression(tokens.drop(consumed)).getOrElse {
            return Outcome.Error(
                GrammarFail(
                    it.message,
                    it.category,
                    consumed + it.consumed,
                ),
            )
        }
        consumed += thenBlock.consumed

        val elseResult = parseOptionalElse(tokens.drop(consumed), table)
        if (elseResult is Outcome.Error) {
            return Outcome.Error(
                GrammarFail(
                    elseResult.error.message,
                    elseResult.error.category,
                    consumed + elseResult.error.consumed,
                ),
            )
        }
        val elseBlock = (elseResult as Outcome.Ok).value
        if (elseBlock is Option.Some) {
            consumed += elseBlock.value.consumed
        }

        val node = buildNode(
            ifToken,
            lparen,
            condition,
            rparen,
            thenBlock,
            elseBlock,
            tokens,
        )
        return Outcome.Ok(GrammarMatch(node, consumed))
    }

    private fun parseOptionalElse(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<Option<GrammarMatch>, GrammarFail> {
        if (tokens.isEmpty() || tokens[0].type != TokenType.ELSE) return Outcome.Ok(Option.None)

        var consumed = 0
        val elseToken = tokens[0]
        consumed += 1

        val elseBlockExpr = table.dispatchExpression(tokens.drop(consumed)).getOrElse {
            return Outcome.Error(
                GrammarFail(
                    it.message,
                    it.category,
                    consumed + it.consumed,
                ),
            )
        }
        consumed += elseBlockExpr.consumed

        val elseNode = Node.Composite(
            children = listOf(
                Node.Leaf(
                    ElseNode,
                    StringValue(elseToken.lexeme),
                    elseToken.span,
                    elseToken.leading,
                    elseToken.trailing,
                ),
                elseBlockExpr.node,
            ),
            type = ElseBlockNode,
            span = Span(elseToken.span.start, elseBlockExpr.node.span.end),
        )

        return Outcome.Ok(Option.Some(GrammarMatch(elseNode, consumed)))
    }

    private fun buildNode(
        ifToken: Token,
        lparen: Token,
        condition: GrammarMatch,
        rparen: Token,
        thenBlock: GrammarMatch,
        elseBlock: Option<GrammarMatch>,
        tokens: List<Token>,
    ): Node {
        val children = buildList {
            add(
                Node.Leaf(
                    IfNode,
                    StringValue(ifToken.lexeme),
                    ifToken.span,
                    ifToken.leading,
                    ifToken.trailing,
                ),
            )
            add(
                Node.Leaf(
                    LeftParenthesisNode,
                    StringValue(lparen.lexeme),
                    lparen.span,
                    lparen.leading,
                    lparen.trailing,
                ),
            )
            add(condition.node)
            add(
                Node.Leaf(
                    RightParenthesisNode,
                    StringValue(rparen.lexeme),
                    rparen.span,
                    rparen.leading,
                    rparen.trailing,
                ),
            )
            add(thenBlock.node)

            if (elseBlock is Option.Some) add(elseBlock.value.node)
        }

        val endSpan = if (elseBlock is Option.Some) {
            elseBlock.value.node.span.end
        } else {
            thenBlock.node.span.end
        }

        return Node.Composite(
            children = children,
            type = type,
            span = Span(tokens.first().span.start, endSpan),
        )
    }
}

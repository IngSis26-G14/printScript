package parser.internal.model.grammar.expression

import common.model.node.BinaryOperationExpressionNode
import common.model.node.Node
import common.model.node.NodeType
import common.model.span.Span
import common.model.token.Token
import common.model.token.TokenType
import common.model.value.StringValue
import parser.internal.model.category.MissingOperand
import parser.internal.model.grammar.GrammarFail
import parser.internal.model.grammar.GrammarMatch
import parser.internal.model.operator.BinaryOperator
import parser.internal.table.GrammarTable
import common.type.outcome.Outcome

internal class BinaryOperationExpression(
    private val operators: Map<TokenType, BinaryOperator>,
) : Expression {
    override val type = BinaryOperationExpressionNode

    override fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail> {
        return parseExpression(tokens, table, 0, 0)
    }

    private fun parseExpression(
        tokens: List<Token>,
        table: GrammarTable,
        startIndex: Int,
        minPrecedence: Int,
    ): Outcome<GrammarMatch, GrammarFail> {
        val lhsOutcome = table.dispatchPrimary(tokens.subList(startIndex, tokens.size))
        if (lhsOutcome is Outcome.Error) return lhsOutcome

        var current = (lhsOutcome as Outcome.Ok).value
        var index = startIndex + current.consumed

        while (index < tokens.size) {
            val operator = tokens[index]
            val operatorInfo = operators[operator.type] ?: break
            val (operatorNodeType, precedence) = operatorInfo

            if (precedence < minPrecedence) break

            if (index + 1 >= tokens.size) {
                return Outcome.Error(
                    GrammarFail(
                        "Missing operand after operator '${operator.lexeme}'",
                        MissingOperand,
                        current.consumed,
                    ),
                )
            }

            val rhsOutcome = parseExpression(
                tokens,
                table,
                index + 1,
                precedence + 1,
            )

            val rhs = when (rhsOutcome) {
                is Outcome.Ok -> rhsOutcome.value
                is Outcome.Error -> return rhsOutcome
            }

            current = GrammarMatch(
                buildNode(current.node, operator, operatorNodeType, rhs.node),
                current.consumed + 1 + rhs.consumed,
            )
            index += 1 + rhs.consumed
        }

        return Outcome.Ok(current)
    }

    private fun buildNode(
        lhs: Node,
        operator: Token,
        operatorNodeType: NodeType,
        rhs: Node,
    ): Node {
        return Node.Composite(
            children = listOf(
                lhs,
                Node.Leaf(
                    operatorNodeType,
                    StringValue(operator.lexeme),
                    operator.span,
                    operator.leading,
                    operator.trailing,
                ),
                rhs,
            ),
            type = type,
            span = Span(lhs.span.start, rhs.span.end),
        )
    }
}

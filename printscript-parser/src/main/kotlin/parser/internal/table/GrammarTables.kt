@file:Suppress("ktlint:standard:filename")

package parser.internal.table

import common.model.node.DivideNode
import common.model.node.MinusNode
import common.model.node.MultiplyNode
import common.model.node.NumberTypeNode
import common.model.node.PlusNode
import common.model.node.StringTypeNode
import common.model.token.TokenType
import parser.internal.model.grammar.expression.BinaryOperationExpression
import parser.internal.model.grammar.expression.Expression
import parser.internal.model.grammar.primary.IdentifierPrimary
import parser.internal.model.grammar.primary.NumberLiteralPrimary
import parser.internal.model.grammar.primary.ParenthesizedPrimary
import parser.internal.model.grammar.primary.Primary
import parser.internal.model.grammar.primary.StringLiteralPrimary
import parser.internal.model.grammar.primary.UnaryOperationPrimary
import parser.internal.model.grammar.statement.AssignStatement
import parser.internal.model.grammar.statement.LetDeclarationStatement
import parser.internal.model.grammar.statement.PrintlnStatement
import parser.internal.model.grammar.statement.Statement
import parser.internal.model.operator.BinaryOperator

internal object PrintScriptV10 : GrammarTable {
    override val statements: Collection<Statement> = listOf(
        AssignStatement(),
        LetDeclarationStatement(
            mapOf(
                TokenType.TYPE_NUMBER to NumberTypeNode,
                TokenType.TYPE_STRING to StringTypeNode,
            ),
        ),
        PrintlnStatement(),
    )

    override val primaries: Collection<Primary> = listOf(
        IdentifierPrimary(),
        NumberLiteralPrimary(),
        ParenthesizedPrimary(),
        StringLiteralPrimary(),
        UnaryOperationPrimary(
            mapOf(
                TokenType.SUBTRACTION_OPERATOR to MinusNode,
                TokenType.ADDITION_OPERATOR to PlusNode,
            ),
        ),
    )

    override val expressions: Collection<Expression> = listOf(
        BinaryOperationExpression(
            mapOf(
                TokenType.DIVISION_OPERATOR to BinaryOperator(DivideNode, 2),
                TokenType.SUBTRACTION_OPERATOR to BinaryOperator(MinusNode, 1),
                TokenType.MULTIPLICATION_OPERATOR to BinaryOperator(MultiplyNode, 2),
                TokenType.ADDITION_OPERATOR to BinaryOperator(PlusNode, 1),
            ),
        ),
    )
}

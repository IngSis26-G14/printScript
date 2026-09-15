package util

import common.model.node.AssignNode
import common.model.node.AssignStatementNode
import common.model.node.BinaryOperationExpressionNode
import common.model.node.ColonNode
import common.model.node.IdentifierNode
import common.model.node.LeftParenthesisNode
import common.model.node.LetDeclarationStatementNode
import common.model.node.LetNode
import common.model.node.MinusNode
import common.model.node.MultiplyNode
import common.model.node.Node
import common.model.node.NumberLiteralNode
import common.model.node.NumberTypeNode
import common.model.node.PlusNode
import common.model.node.PrintlnKeywordNode
import common.model.node.PrintlnStatementNode
import common.model.node.RightParenthesisNode
import common.model.node.SemicolonNode
import common.model.node.StringLiteralNode
import common.model.node.StringTypeNode
import common.model.node.UnaryOperationNode
import common.model.span.Position
import common.model.span.Span
import common.model.value.IntegerValue
import common.model.value.StringValue

internal object PrintScriptV10TestSnippets {

    init {

        TestSnippetRegistry.register(
            "empty-nodes",
            listOf(),
        )

        TestSnippetRegistry.register(
            "identifier-redeclaration",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 14, 13)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(1, 1, 0), Position(1, 4, 3))),
                        Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(1, 5, 4), Position(1, 6, 5))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 6, 5), Position(1, 7, 6))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(1, 8, 7), Position(1, 14, 13))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(1, 15, 14), Position(1, 16, 15))),
                        Node.Leaf(NumberLiteralNode, IntegerValue(5), Span(Position(1, 17, 16), Position(1, 18, 17))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(1, 18, 17), Position(1, 19, 18))),
                    ),
                ),
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 19), Position(2, 15, 33)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(2, 1, 19), Position(2, 4, 22))),
                        Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(2, 5, 23), Position(2, 6, 24))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(2, 6, 24), Position(2, 7, 25))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(2, 8, 26), Position(2, 14, 32))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(2, 15, 33), Position(2, 16, 34))),
                        Node.Leaf(NumberLiteralNode, IntegerValue(10), Span(Position(2, 17, 35), Position(2, 19, 37))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(2, 19, 37), Position(2, 20, 38))),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "invalid-string-binary-operation",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 21, 20)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(1, 1, 0), Position(1, 4, 3))),
                        Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(1, 5, 4), Position(1, 6, 5))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 6, 5), Position(1, 7, 6))),
                        Node.Leaf(StringTypeNode, StringValue("string"), Span(Position(1, 8, 7), Position(1, 14, 13))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(1, 15, 14), Position(1, 16, 15))),
                        Node.Leaf(StringLiteralNode, StringValue("Hello"), Span(Position(1, 17, 16), Position(1, 22, 21))),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 22), Position(2, 26, 47)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(2, 1, 22), Position(2, 4, 25))),
                        Node.Leaf(IdentifierNode, StringValue("y"), Span(Position(2, 5, 26), Position(2, 6, 27))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(2, 6, 27), Position(2, 7, 28))),
                        Node.Leaf(StringTypeNode, StringValue("string"), Span(Position(2, 8, 29), Position(2, 14, 35))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(2, 15, 36), Position(2, 16, 37))),
                        Node.Leaf(StringLiteralNode, StringValue("Goodbye"), Span(Position(2, 17, 38), Position(2, 26, 47))),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(3, 1, 48), Position(3, 22, 69)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(3, 1, 48), Position(3, 4, 51))),
                        Node.Leaf(IdentifierNode, StringValue("z"), Span(Position(3, 5, 52), Position(3, 6, 53))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(3, 6, 53), Position(3, 7, 54))),
                        Node.Leaf(StringTypeNode, StringValue("string"), Span(Position(3, 8, 55), Position(3, 14, 61))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(3, 15, 62), Position(3, 16, 63))),

                        Node.Composite(
                            type = BinaryOperationExpressionNode,
                            span = Span(Position(3, 17, 64), Position(3, 22, 69)),
                            children = listOf(
                                Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(3, 17, 64), Position(3, 18, 65))),
                                Node.Leaf(MultiplyNode, StringValue("*"), Span(Position(3, 19, 66), Position(3, 20, 67))),
                                Node.Leaf(IdentifierNode, StringValue("y"), Span(Position(3, 21, 68), Position(3, 22, 69))),
                            ),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "invalid-string-unary-operation",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 23, 22)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(1, 1, 0), Position(1, 4, 3))),
                        Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(1, 5, 4), Position(1, 6, 5))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 6, 5), Position(1, 7, 6))),
                        Node.Leaf(StringTypeNode, StringValue("string"), Span(Position(1, 8, 7), Position(1, 14, 13))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(1, 15, 14), Position(1, 16, 15))),
                        Node.Composite(
                            type = UnaryOperationNode,
                            span = Span(Position(1, 17, 16), Position(1, 22, 21)),
                            children = listOf(
                                Node.Leaf(MinusNode, StringValue("-"), Span(Position(1, 17, 16), Position(1, 18, 17))),
                                Node.Leaf(StringLiteralNode, StringValue("\"Hello, World!\""), Span(Position(1, 18, 17), Position(1, 22, 21))),
                            ),
                        ),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(1, 22, 21), Position(1, 23, 22))),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "invalid-type-assignation",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 27, 26)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(1, 1, 0), Position(1, 4, 3))),
                        Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(1, 5, 4), Position(1, 6, 5))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 6, 5), Position(1, 7, 6))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(1, 8, 7), Position(1, 14, 13))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(1, 15, 14), Position(1, 16, 15))),
                        Node.Leaf(StringLiteralNode, StringValue("\"Hello, World!\""), Span(Position(1, 17, 16), Position(1, 27, 26))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(1, 27, 26), Position(1, 28, 27))),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 28), Position(2, 17, 44)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(2, 1, 28), Position(2, 4, 31))),
                        Node.Leaf(IdentifierNode, StringValue("y"), Span(Position(2, 5, 32), Position(2, 6, 33))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(2, 6, 33), Position(2, 7, 34))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(2, 8, 35), Position(2, 14, 41))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(2, 15, 42), Position(2, 16, 43))),
                        Node.Leaf(NumberLiteralNode, IntegerValue(5), Span(Position(2, 17, 44), Position(2, 18, 45))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(2, 18, 45), Position(2, 19, 46))),
                    ),
                ),

                Node.Composite(
                    type = AssignStatementNode,
                    span = Span(Position(3, 1, 46), Position(3, 25, 70)),
                    children = listOf(
                        Node.Leaf(IdentifierNode, StringValue("y"), Span(Position(3, 1, 46), Position(3, 2, 47))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(3, 3, 48), Position(3, 4, 49))),
                        Node.Leaf(StringLiteralNode, StringValue("\"Goodbye, World!\""), Span(Position(3, 5, 50), Position(3, 25, 70))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(3, 25, 70), Position(3, 26, 71))),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "none-value",
            listOf(
                // let x: number;
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 14, 13)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(1, 1, 0), Position(1, 4, 3))),
                        Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(1, 5, 4), Position(1, 6, 5))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 6, 5), Position(1, 7, 6))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(1, 8, 7), Position(1, 14, 13))),
                    ),
                ),

                Node.Composite(
                    type = AssignStatementNode,
                    span = Span(Position(2, 1, 14), Position(2, 10, 23)),
                    children = listOf(
                        Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(2, 1, 14), Position(2, 2, 15))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(2, 3, 16), Position(2, 4, 17))),
                        Node.Composite(
                            type = BinaryOperationExpressionNode,
                            span = Span(Position(2, 5, 18), Position(2, 10, 23)),
                            children = listOf(
                                Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(2, 5, 18), Position(2, 6, 19))),
                                Node.Leaf(PlusNode, StringValue("+"), Span(Position(2, 7, 20), Position(2, 8, 21))),
                                Node.Leaf(NumberLiteralNode, IntegerValue(5), Span(Position(2, 9, 22), Position(2, 10, 23))),
                            ),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "undefined-identifier",
            listOf(
                Node.Composite(
                    type = PrintlnStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 11, 10)),
                    children = listOf(
                        Node.Leaf(PrintlnKeywordNode, StringValue("println"), Span(Position(1, 1, 0), Position(1, 8, 7))),
                        Node.Leaf(LeftParenthesisNode, StringValue("("), Span(Position(1, 8, 7), Position(1, 9, 8))),
                        Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(1, 9, 8), Position(1, 10, 9))),
                        Node.Leaf(RightParenthesisNode, StringValue(")"), Span(Position(1, 10, 9), Position(1, 11, 10))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(1, 11, 10), Position(1, 12, 11))),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "valid-statements",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 14, 13)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(1, 1, 0), Position(1, 4, 3))),
                        Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(1, 5, 4), Position(1, 6, 5))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 6, 5), Position(1, 7, 6))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(1, 8, 7), Position(1, 14, 13))),
                    ),
                ),

                Node.Composite(
                    type = AssignStatementNode,
                    span = Span(Position(2, 1, 14), Position(2, 6, 19)),
                    children = listOf(
                        Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(2, 1, 14), Position(2, 2, 15))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(2, 3, 16), Position(2, 4, 17))),
                        Node.Leaf(NumberLiteralNode, IntegerValue(5), Span(Position(2, 5, 18), Position(2, 6, 19))),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(3, 1, 20), Position(3, 18, 37)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(3, 1, 20), Position(3, 4, 23))),
                        Node.Leaf(IdentifierNode, StringValue("y"), Span(Position(3, 5, 24), Position(3, 6, 25))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(3, 6, 25), Position(3, 7, 26))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(3, 8, 27), Position(3, 14, 33))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(3, 15, 34), Position(3, 16, 35))),
                        Node.Leaf(NumberLiteralNode, IntegerValue(6), Span(Position(3, 17, 36), Position(3, 18, 37))),
                    ),
                ),

                Node.Composite(
                    type = PrintlnStatementNode,
                    span = Span(Position(4, 1, 38), Position(4, 27, 64)),
                    children = listOf(
                        Node.Leaf(PrintlnKeywordNode, StringValue("println"), Span(Position(4, 1, 38), Position(4, 8, 45))),
                        Node.Leaf(LeftParenthesisNode, StringValue("("), Span(Position(4, 8, 45), Position(4, 9, 46))),
                        Node.Composite(
                            type = BinaryOperationExpressionNode,
                            span = Span(Position(4, 9, 46), Position(4, 26, 63)),
                            children = listOf(
                                Node.Leaf(StringLiteralNode, StringValue("\"Result: \""), Span(Position(4, 9, 46), Position(4, 18, 55))),
                                Node.Leaf(PlusNode, StringValue("+"), Span(Position(4, 19, 56), Position(4, 20, 57))),
                                Node.Leaf(IdentifierNode, StringValue("x"), Span(Position(4, 21, 58), Position(4, 22, 59))),
                                Node.Leaf(PlusNode, StringValue("+"), Span(Position(4, 23, 60), Position(4, 24, 61))),
                                Node.Leaf(IdentifierNode, StringValue("y"), Span(Position(4, 25, 62), Position(4, 26, 63))),
                            ),
                        ),
                        Node.Leaf(RightParenthesisNode, StringValue(")"), Span(Position(4, 26, 63), Position(4, 27, 64))),
                    ),
                ),
            ),
        )
    }
}

package util

import common.model.node.AssignNode
import common.model.node.AssignStatementNode
import common.model.node.BinaryOperationExpressionNode
import common.model.node.ColonNode
import common.model.node.IdentifierNode
import common.model.node.LeftParenthesisNode
import common.model.node.LetDeclarationStatementNode
import common.model.node.LetNode
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
import common.model.span.Position
import common.model.span.Span
import common.model.value.FloatValue
import common.model.value.StringValue

internal object PrintScriptV10TestSnippets {

    init {
        TestSnippetRegistry.register(
            "expression-in-println",
            listOf(
                Node.Composite(
                    type = PrintlnStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 26, 25)),
                    children = listOf(
                        Node.Leaf(PrintlnKeywordNode, StringValue("println"), Span(Position(1, 1, 0), Position(1, 8, 7))),
                        Node.Leaf(LeftParenthesisNode, StringValue("("), Span(Position(1, 8, 7), Position(1, 9, 8))),
                        Node.Composite(
                            type = BinaryOperationExpressionNode,
                            span = Span(Position(1, 9, 8), Position(1, 25, 24)),
                            children = listOf(
                                Node.Leaf(StringLiteralNode, StringValue("Hello, "), Span(Position(1, 9, 8), Position(1, 17, 16))),
                                Node.Leaf(PlusNode, StringValue("+"), Span(Position(1, 17, 16), Position(1, 18, 17))),
                                Node.Leaf(StringLiteralNode, StringValue("World!"), Span(Position(1, 19, 18), Position(1, 25, 24))),
                            ),
                        ),
                        Node.Leaf(RightParenthesisNode, StringValue(")"), Span(Position(1, 25, 24), Position(1, 26, 25))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(1, 26, 25), Position(1, 27, 26))),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "identifier-camel-case",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 21, 20)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(1, 1, 0), Position(1, 4, 3))),
                        Node.Leaf(IdentifierNode, StringValue("myVar"), Span(Position(1, 5, 4), Position(1, 10, 9))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 10, 9), Position(1, 11, 10))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(1, 12, 11), Position(1, 18, 17))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(1, 19, 18), Position(1, 20, 19))),
                        Node.Leaf(NumberLiteralNode, FloatValue(5f), Span(Position(1, 21, 20), Position(1, 21, 20))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(1, 21, 20), Position(1, 22, 21))),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 22), Position(2, 22, 43)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("ley"), Span(Position(2, 1, 22), Position(2, 4, 25))),
                        Node.Leaf(IdentifierNode, StringValue("my_var"), Span(Position(2, 5, 26), Position(2, 11, 32))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(2, 11, 32), Position(2, 12, 33))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(2, 13, 34), Position(2, 19, 40))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(2, 20, 41), Position(2, 21, 42))),
                        Node.Leaf(NumberLiteralNode, FloatValue(5f), Span(Position(2, 22, 43), Position(2, 22, 43))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(2, 22, 43), Position(2, 23, 44))),
                    ),
                ),

                Node.Composite(
                    type = AssignStatementNode,
                    span = Span(Position(3, 1, 44), Position(3, 12, 55)),
                    children = listOf(
                        Node.Leaf(IdentifierNode, StringValue("my_var"), Span(Position(3, 1, 44), Position(3, 7, 50))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(3, 8, 51), Position(3, 9, 52))),
                        Node.Leaf(NumberLiteralNode, FloatValue(10f), Span(Position(3, 10, 53), Position(3, 11, 54))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(3, 12, 55), Position(3, 13, 56))),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "identifier-snake-case",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 22, 21)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("ley"), Span(Position(1, 1, 0), Position(1, 4, 3))),
                        Node.Leaf(IdentifierNode, StringValue("my_var"), Span(Position(1, 5, 4), Position(1, 11, 10))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 11, 10), Position(1, 12, 11))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(1, 13, 12), Position(1, 19, 18))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(1, 20, 19), Position(1, 21, 20))),
                        Node.Leaf(NumberLiteralNode, FloatValue(5f), Span(Position(1, 22, 21), Position(1, 22, 21))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(1, 22, 21), Position(1, 23, 22))),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 23), Position(2, 21, 43)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(2, 1, 23), Position(2, 4, 26))),
                        Node.Leaf(IdentifierNode, StringValue("myVar"), Span(Position(2, 5, 27), Position(2, 10, 32))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(2, 10, 32), Position(2, 11, 33))),
                        Node.Leaf(NumberTypeNode, StringValue("number"), Span(Position(2, 12, 34), Position(2, 18, 40))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(2, 19, 41), Position(2, 20, 42))),
                        Node.Leaf(NumberLiteralNode, FloatValue(5f), Span(Position(2, 21, 43), Position(2, 21, 43))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(2, 21, 43), Position(2, 22, 44))),
                    ),
                ),

                Node.Composite(
                    type = AssignStatementNode,
                    span = Span(Position(3, 1, 44), Position(3, 12, 55)),
                    children = listOf(
                        Node.Leaf(IdentifierNode, StringValue("myVar"), Span(Position(3, 1, 44), Position(3, 6, 49))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(3, 7, 50), Position(3, 8, 51))),
                        Node.Leaf(NumberLiteralNode, FloatValue(10f), Span(Position(3, 9, 52), Position(3, 10, 53))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(3, 11, 54), Position(3, 12, 55))),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "invalid-rule-value",
            listOf(),
        )

        TestSnippetRegistry.register(
            "valid-println",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 38, 37)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("let"), Span(Position(1, 1, 0), Position(1, 4, 3))),
                        Node.Leaf(IdentifierNode, StringValue("message"), Span(Position(1, 5, 4), Position(1, 12, 11))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 12, 11), Position(1, 13, 12))),
                        Node.Leaf(StringTypeNode, StringValue("string"), Span(Position(1, 14, 13), Position(1, 20, 19))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(1, 21, 20), Position(1, 22, 21))),
                        Node.Composite(
                            type = BinaryOperationExpressionNode,
                            span = Span(Position(1, 23, 22), Position(1, 37, 36)),
                            children = listOf(
                                Node.Leaf(StringLiteralNode, StringValue("Hello, "), Span(Position(1, 23, 22), Position(1, 32, 31))),
                                Node.Leaf(PlusNode, StringValue("+"), Span(Position(1, 33, 32), Position(1, 34, 33))),
                                Node.Leaf(StringLiteralNode, StringValue("World!"), Span(Position(1, 35, 34), Position(1, 37, 36))),
                            ),
                        ),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(1, 37, 36), Position(1, 38, 37))),
                    ),
                ),

                Node.Composite(
                    type = PrintlnStatementNode,
                    span = Span(Position(2, 1, 38), Position(2, 16, 53)),
                    children = listOf(
                        Node.Leaf(PrintlnKeywordNode, StringValue("println"), Span(Position(2, 1, 38), Position(2, 8, 45))),
                        Node.Leaf(LeftParenthesisNode, StringValue("("), Span(Position(2, 9, 46), Position(2, 10, 47))),
                        Node.Leaf(IdentifierNode, StringValue("message"), Span(Position(2, 10, 47), Position(2, 17, 54))),
                        Node.Leaf(RightParenthesisNode, StringValue(")"), Span(Position(2, 17, 54), Position(2, 18, 55))),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(2, 18, 55), Position(2, 19, 56))),
                    ),
                ),
            ),
        )
    }
}

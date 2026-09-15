package util

import common.model.node.AssignNode
import common.model.node.BinaryOperationExpressionNode
import common.model.node.ColonNode
import common.model.node.ConstDeclarationStatementNode
import common.model.node.ConstNode
import common.model.node.IdentifierNode
import common.model.node.LeftParenthesisNode
import common.model.node.LetDeclarationStatementNode
import common.model.node.LetNode
import common.model.node.Node
import common.model.node.PlusNode
import common.model.node.ReadInputExpressionNode
import common.model.node.ReadInputKeywordNode
import common.model.node.RightParenthesisNode
import common.model.node.SemicolonNode
import common.model.node.StringLiteralNode
import common.model.node.StringTypeNode
import common.model.span.Position
import common.model.span.Span
import common.model.value.StringValue

object PrintScriptV11TestSnippets {

    init {
        TestSnippetRegistry.register(
            "expression-in-readInput",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 40, 39)),
                    children = listOf(
                        Node.Leaf(LetNode, StringValue("const"), Span(Position(1, 1, 0), Position(1, 6, 5))),
                        Node.Leaf(IdentifierNode, StringValue("name"), Span(Position(1, 7, 6), Position(1, 11, 10))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 11, 10), Position(1, 12, 11))),
                        Node.Leaf(StringTypeNode, StringValue("string"), Span(Position(1, 13, 12), Position(1, 19, 18))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(1, 20, 19), Position(1, 21, 20))),

                        Node.Composite(
                            type = ReadInputExpressionNode,
                            span = Span(Position(1, 22, 21), Position(1, 40, 39)),
                            children = listOf(
                                Node.Leaf(ReadInputKeywordNode, StringValue("readInput"), Span(Position(1, 22, 21), Position(1, 30, 29))),
                                Node.Leaf(LeftParenthesisNode, StringValue("("), Span(Position(1, 30, 29), Position(1, 31, 30))),

                                Node.Composite(
                                    type = BinaryOperationExpressionNode,
                                    span = Span(Position(1, 31, 30), Position(1, 39, 38)),
                                    children = listOf(
                                        Node.Leaf(StringLiteralNode, StringValue("Enter "), Span(Position(1, 31, 30), Position(1, 39, 38))),
                                        Node.Leaf(PlusNode, StringValue("+"), Span(Position(1, 39, 38), Position(1, 40, 39))),
                                        Node.Leaf(StringLiteralNode, StringValue("name: "), Span(Position(1, 41, 40), Position(1, 47, 46))),
                                    ),
                                ),

                                Node.Leaf(RightParenthesisNode, StringValue(")"), Span(Position(1, 47, 46), Position(1, 48, 47))),
                            ),
                        ),

                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(1, 48, 47), Position(1, 49, 48))),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "valid-readInput",
            listOf(
                Node.Composite(
                    type = ConstDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 32, 31)),
                    children = listOf(
                        Node.Leaf(ConstNode, StringValue("const"), Span(Position(1, 1, 0), Position(1, 6, 5))),
                        Node.Leaf(IdentifierNode, StringValue("prompt"), Span(Position(1, 7, 6), Position(1, 13, 12))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(1, 13, 12), Position(1, 14, 13))),
                        Node.Leaf(StringTypeNode, StringValue("string"), Span(Position(1, 15, 14), Position(1, 21, 20))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(1, 22, 21), Position(1, 23, 22))),
                        Node.Composite(
                            type = BinaryOperationExpressionNode,
                            span = Span(Position(1, 24, 23), Position(1, 32, 31)),
                            children = listOf(
                                Node.Leaf(StringLiteralNode, StringValue("Enter "), Span(Position(1, 24, 23), Position(1, 31, 30))),
                                Node.Leaf(PlusNode, StringValue("+"), Span(Position(1, 31, 30), Position(1, 32, 31))),
                                Node.Leaf(StringLiteralNode, StringValue("name:"), Span(Position(1, 33, 32), Position(1, 39, 38))),
                            ),
                        ),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(1, 39, 38), Position(1, 40, 39))),
                    ),
                ),

                Node.Composite(
                    type = ConstDeclarationStatementNode,
                    span = Span(Position(2, 1, 41), Position(2, 32, 72)),
                    children = listOf(
                        Node.Leaf(ConstNode, StringValue("const"), Span(Position(2, 1, 41), Position(2, 6, 46))),
                        Node.Leaf(IdentifierNode, StringValue("name"), Span(Position(2, 7, 47), Position(2, 11, 51))),
                        Node.Leaf(ColonNode, StringValue(":"), Span(Position(2, 11, 51), Position(2, 12, 52))),
                        Node.Leaf(StringTypeNode, StringValue("string"), Span(Position(2, 13, 53), Position(2, 19, 59))),
                        Node.Leaf(AssignNode, StringValue("="), Span(Position(2, 20, 60), Position(2, 21, 61))),
                        Node.Composite(
                            type = ReadInputExpressionNode,
                            span = Span(Position(2, 22, 62), Position(2, 32, 72)),
                            children = listOf(
                                Node.Leaf(ReadInputKeywordNode, StringValue("readInput"), Span(Position(2, 22, 62), Position(2, 31, 71))),
                                Node.Leaf(LeftParenthesisNode, StringValue("("), Span(Position(2, 31, 71), Position(2, 32, 72))),
                                Node.Leaf(IdentifierNode, StringValue("prompt"), Span(Position(2, 32, 72), Position(2, 38, 78))),
                                Node.Leaf(RightParenthesisNode, StringValue(")"), Span(Position(2, 38, 78), Position(2, 39, 79))),
                            ),
                        ),
                        Node.Leaf(SemicolonNode, StringValue(";"), Span(Position(2, 39, 79), Position(2, 40, 80))),
                    ),
                ),
            ),
        )
    }
}

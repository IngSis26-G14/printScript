package util

import common.model.node.BlockNode
import common.model.node.IdentifierNode
import common.model.node.IfNode
import common.model.node.IfStatementNode
import common.model.node.LeftBraceNode
import common.model.node.LeftParenthesisNode
import common.model.node.Node
import common.model.node.NumberLiteralNode
import common.model.node.PrintlnKeywordNode
import common.model.node.PrintlnStatementNode
import common.model.node.RightBraceNode
import common.model.node.RightParenthesisNode
import common.model.node.SemicolonNode
import common.model.node.StringLiteralNode
import common.model.span.Position
import common.model.span.Span
import common.model.trivia.NewlineTrivia
import common.model.trivia.SpaceTrivia
import common.model.trivia.Trivia
import common.model.value.StringValue

object PrintScriptV11TestSnippets {

    init {
        TestSnippetRegistry.register(
            "if-brace-below-line",
            listOf(
                Node.Composite(
                    type = IfStatementNode,
                    span = Span(Position(1, 1, 0), Position(3, 2, 20)),
                    children = listOf(
                        Node.Leaf(
                            type = IfNode,
                            value = StringValue("if"),
                            span = Span(Position(1, 1, 0), Position(1, 3, 2)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 3, 2), Position(1, 4, 3)))),
                        ),
                        Node.Leaf(
                            type = LeftParenthesisNode,
                            value = StringValue("("),
                            span = Span(Position(1, 4, 3), Position(1, 5, 4)),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = RightParenthesisNode,
                            value = StringValue(")"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                        ),
                        Node.Leaf(
                            type = LeftBraceNode,
                            value = StringValue("{"),
                            span = Span(Position(1, 8, 7), Position(1, 9, 8)),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 9, 8), Position(2, 3, 12)))),
                        ),

                        Node.Composite(
                            type = PrintlnStatementNode,
                            span = Span(Position(2, 3, 12), Position(2, 13, 22)),
                            children = listOf(
                                Node.Leaf(
                                    type = PrintlnKeywordNode,
                                    value = StringValue("println"),
                                    span = Span(Position(2, 3, 12), Position(2, 10, 19)),
                                    leading = listOf(
                                        Trivia(SpaceTrivia, " ", Span(Position(1, 9, 8), Position(2, 3, 12))),
                                        Trivia(SpaceTrivia, " ", Span(Position(1, 9, 8), Position(2, 3, 12))),
                                    ),
                                ),
                                Node.Leaf(
                                    type = LeftParenthesisNode,
                                    value = StringValue("("),
                                    span = Span(Position(2, 10, 19), Position(2, 11, 20)),
                                ),
                                Node.Leaf(
                                    type = NumberLiteralNode,
                                    value = StringValue("5"),
                                    span = Span(Position(2, 11, 20), Position(2, 12, 21)),
                                ),
                                Node.Leaf(
                                    type = RightParenthesisNode,
                                    value = StringValue(")"),
                                    span = Span(Position(2, 12, 21), Position(2, 13, 22)),
                                ),
                                Node.Leaf(
                                    type = SemicolonNode,
                                    value = StringValue(";"),
                                    span = Span(Position(2, 13, 22), Position(2, 14, 23)),
                                    trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(2, 14, 23), Position(3, 1, 18)))),
                                ),
                            ),
                        ),

                        Node.Leaf(
                            type = RightBraceNode,
                            value = StringValue("}"),
                            span = Span(Position(3, 1, 18), Position(3, 2, 19)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "if-brace-same-line",
            listOf(
                Node.Composite(
                    type = IfStatementNode,
                    span = Span(Position(1, 1, 0), Position(3, 15, 28)),
                    children = listOf(
                        Node.Leaf(
                            type = IfNode,
                            value = StringValue("if"),
                            span = Span(Position(1, 1, 0), Position(1, 3, 2)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 3, 2), Position(1, 4, 3)))),
                        ),
                        Node.Leaf(
                            type = LeftParenthesisNode,
                            value = StringValue("("),
                            span = Span(Position(1, 4, 3), Position(1, 5, 4)),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = RightParenthesisNode,
                            value = StringValue(")"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                        ),
                        Node.Leaf(
                            type = LeftBraceNode,
                            value = StringValue("{"),
                            span = Span(Position(1, 8, 7), Position(1, 9, 8)),
                            leading = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 9, 8), Position(2, 3, 12)))),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 9, 8), Position(2, 3, 12)))),
                        ),

                        Node.Composite(
                            type = PrintlnStatementNode,
                            span = Span(Position(2, 3, 12), Position(2, 13, 22)),
                            children = listOf(
                                Node.Leaf(
                                    type = PrintlnKeywordNode,
                                    value = StringValue("println"),
                                    span = Span(Position(2, 3, 12), Position(2, 10, 19)),
                                    leading = listOf(
                                        Trivia(SpaceTrivia, " ", Span(Position(1, 9, 8), Position(2, 3, 12))),
                                        Trivia(SpaceTrivia, " ", Span(Position(1, 9, 8), Position(2, 3, 12))),
                                    ),
                                ),
                                Node.Leaf(
                                    type = LeftParenthesisNode,
                                    value = StringValue("("),
                                    span = Span(Position(2, 10, 19), Position(2, 11, 20)),
                                ),
                                Node.Leaf(
                                    type = NumberLiteralNode,
                                    value = StringValue("5"),
                                    span = Span(Position(2, 11, 20), Position(2, 12, 21)),
                                ),
                                Node.Leaf(
                                    type = RightParenthesisNode,
                                    value = StringValue(")"),
                                    span = Span(Position(2, 12, 21), Position(2, 13, 22)),
                                ),
                                Node.Leaf(
                                    type = SemicolonNode,
                                    value = StringValue(";"),
                                    span = Span(Position(2, 13, 22), Position(2, 14, 23)),
                                    trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(2, 14, 23), Position(3, 1, 23)))),
                                ),
                            ),
                        ),
                        Node.Leaf(
                            type = RightBraceNode,
                            value = StringValue("}"),
                            span = Span(Position(1, 8, 7), Position(1, 9, 8)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "indents-in-if",
            listOf(
                Node.Composite(
                    type = IfStatementNode,
                    span = Span(Position(1, 1, 0), Position(5, 2, 39)),
                    children = listOf(
                        Node.Leaf(
                            type = IfNode,
                            value = StringValue("if"),
                            span = Span(Position(1, 1, 0), Position(1, 3, 2)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 3, 2), Position(1, 4, 3)))),
                        ),
                        Node.Leaf(
                            type = LeftParenthesisNode,
                            value = StringValue("("),
                            span = Span(Position(1, 4, 3), Position(1, 5, 4)),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = RightParenthesisNode,
                            value = StringValue(")"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 7, 6), Position(1, 8, 7)))),
                        ),

                        // BlockNode wrapping the entire then block
                        Node.Composite(
                            type = BlockNode,
                            span = Span(Position(1, 8, 7), Position(5, 2, 39)),
                            children = listOf(
                                Node.Leaf(
                                    type = LeftBraceNode,
                                    value = StringValue("{"),
                                    span = Span(Position(1, 8, 7), Position(1, 9, 8)),
                                    trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 9, 8), Position(2, 1, 9)))),
                                ),

                                // Nested IfStatement inside the block
                                Node.Composite(
                                    type = IfStatementNode,
                                    span = Span(Position(2, 3, 10), Position(4, 4, 38)),
                                    children = listOf(
                                        Node.Leaf(
                                            type = IfNode,
                                            value = StringValue("if"),
                                            span = Span(Position(2, 3, 10), Position(2, 5, 12)),
                                            leading = listOf(
                                                Trivia(SpaceTrivia, "  ", Span(Position(2, 1, 9), Position(2, 3, 10))),
                                            ),
                                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 5, 12), Position(2, 6, 13)))),
                                        ),
                                        Node.Leaf(
                                            type = LeftParenthesisNode,
                                            value = StringValue("("),
                                            span = Span(Position(2, 6, 13), Position(2, 7, 14)),
                                        ),
                                        Node.Leaf(
                                            type = IdentifierNode,
                                            value = StringValue("y"),
                                            span = Span(Position(2, 7, 14), Position(2, 8, 15)),
                                        ),
                                        Node.Leaf(
                                            type = RightParenthesisNode,
                                            value = StringValue(")"),
                                            span = Span(Position(2, 8, 15), Position(2, 9, 16)),
                                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 9, 16), Position(2, 10, 17)))),
                                        ),

                                        // BlockNode for nested if's then block
                                        Node.Composite(
                                            type = BlockNode,
                                            span = Span(Position(2, 10, 17), Position(4, 4, 38)),
                                            children = listOf(
                                                Node.Leaf(
                                                    type = LeftBraceNode,
                                                    value = StringValue("{"),
                                                    span = Span(Position(2, 10, 17), Position(2, 11, 18)),
                                                    trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(2, 11, 18), Position(3, 1, 19)))),
                                                ),

                                                Node.Composite(
                                                    type = PrintlnStatementNode,
                                                    span = Span(Position(3, 5, 23), Position(3, 18, 36)),
                                                    children = listOf(
                                                        Node.Leaf(
                                                            type = PrintlnKeywordNode,
                                                            value = StringValue("println"),
                                                            span = Span(Position(3, 5, 23), Position(3, 12, 30)),
                                                            leading = listOf(
                                                                Trivia(SpaceTrivia, "    ", Span(Position(3, 1, 19), Position(3, 5, 23))),
                                                            ),
                                                        ),
                                                        Node.Leaf(
                                                            type = LeftParenthesisNode,
                                                            value = StringValue("("),
                                                            span = Span(Position(3, 12, 30), Position(3, 13, 31)),
                                                        ),
                                                        Node.Leaf(
                                                            type = StringLiteralNode,
                                                            value = StringValue("\"a\""),
                                                            span = Span(Position(3, 13, 31), Position(3, 16, 34)),
                                                        ),
                                                        Node.Leaf(
                                                            type = RightParenthesisNode,
                                                            value = StringValue(")"),
                                                            span = Span(Position(3, 16, 34), Position(3, 17, 35)),
                                                        ),
                                                        Node.Leaf(
                                                            type = SemicolonNode,
                                                            value = StringValue(";"),
                                                            span = Span(Position(3, 17, 35), Position(3, 18, 36)),
                                                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(3, 18, 36), Position(4, 1, 37)))),
                                                        ),
                                                    ),
                                                ),

                                                Node.Leaf(
                                                    type = RightBraceNode,
                                                    value = StringValue("}"),
                                                    span = Span(Position(4, 3, 37), Position(4, 4, 38)),
                                                    leading = listOf(
                                                        Trivia(SpaceTrivia, "  ", Span(Position(4, 1, 37), Position(4, 3, 37))),
                                                    ),
                                                    trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(4, 4, 38), Position(5, 1, 39)))),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),

                                Node.Leaf(
                                    type = RightBraceNode,
                                    value = StringValue("}"),
                                    span = Span(Position(5, 1, 39), Position(5, 2, 40)),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }
}

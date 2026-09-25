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
import common.model.span.Position
import common.model.span.Span
import common.model.trivia.NewlineTrivia
import common.model.trivia.SpaceTrivia
import common.model.trivia.Trivia
import common.model.value.IntegerValue
import common.model.value.StringValue

internal object PrintScriptV10TestSnippets {

    init {
        TestSnippetRegistry.register(
            "no-spacing-around-equals",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 17, 16)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(1, 1, 0), Position(1, 4, 3)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 4, 3), Position(1, 5, 4)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 7, 6), Position(1, 8, 7)))),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(1, 8, 7), Position(1, 14, 13)),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(1, 14, 13), Position(1, 15, 14)),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = IntegerValue(5),
                            span = Span(Position(1, 15, 14), Position(1, 16, 15)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(1, 16, 15), Position(1, 17, 16)),
                        ),
                    ),
                ),

                Node.Composite(
                    type = AssignStatementNode,
                    span = Span(Position(2, 1, 17), Position(2, 5, 21)),
                    children = listOf(
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("y"),
                            span = Span(Position(2, 1, 17), Position(2, 2, 18)),
                            leading = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 17, 16), Position(2, 1, 17)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(2, 2, 18), Position(2, 3, 19)),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = IntegerValue(6),
                            span = Span(Position(2, 3, 19), Position(2, 4, 20)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(2, 4, 20), Position(2, 5, 21)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "println-linebreaks",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 15, 14)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(1, 1, 0), Position(1, 4, 3)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 4, 3), Position(1, 5, 4)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 7, 6), Position(1, 8, 7)))),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(1, 8, 7), Position(1, 14, 13)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(1, 14, 13), Position(1, 15, 14)),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 15, 14), Position(2, 1, 15)))),
                        ),
                    ),
                ),

                Node.Composite(
                    type = PrintlnStatementNode,
                    span = Span(Position(2, 1, 15), Position(2, 12, 26)),
                    children = listOf(
                        Node.Leaf(
                            type = PrintlnKeywordNode,
                            value = StringValue("println"),
                            span = Span(Position(2, 1, 15), Position(2, 8, 22)),
                        ),
                        Node.Leaf(
                            type = LeftParenthesisNode,
                            value = StringValue("("),
                            span = Span(Position(2, 8, 22), Position(2, 9, 23)),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(2, 9, 23), Position(2, 10, 24)),
                        ),
                        Node.Leaf(
                            type = RightParenthesisNode,
                            value = StringValue(")"),
                            span = Span(Position(2, 10, 24), Position(2, 11, 25)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(2, 11, 25), Position(2, 12, 26)),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(2, 12, 26), Position(3, 1, 27)))),
                        ),
                    ),
                ),

                Node.Composite(
                    type = PrintlnStatementNode,
                    span = Span(Position(3, 1, 27), Position(3, 12, 38)),
                    children = listOf(
                        Node.Leaf(
                            type = PrintlnKeywordNode,
                            value = StringValue("println"),
                            span = Span(Position(3, 1, 27), Position(3, 8, 34)),
                        ),
                        Node.Leaf(
                            type = LeftParenthesisNode,
                            value = StringValue("("),
                            span = Span(Position(3, 8, 34), Position(3, 9, 35)),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(3, 9, 35), Position(3, 10, 36)),
                        ),
                        Node.Leaf(
                            type = RightParenthesisNode,
                            value = StringValue(")"),
                            span = Span(Position(3, 10, 36), Position(3, 11, 37)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(3, 11, 37), Position(3, 12, 38)),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(3, 12, 38), Position(4, 1, 39)))),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(4, 1, 39), Position(4, 15, 53)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(4, 1, 39), Position(4, 4, 42)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(4, 4, 42), Position(4, 5, 43)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("y"),
                            span = Span(Position(4, 5, 43), Position(4, 6, 44)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(4, 6, 44), Position(4, 7, 45)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(4, 7, 45), Position(4, 8, 46)))),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(4, 8, 46), Position(4, 14, 52)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(4, 14, 52), Position(4, 15, 53)),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(4, 15, 53), Position(5, 1, 54)))),
                        ),
                    ),
                ),

                Node.Composite(
                    type = PrintlnStatementNode,
                    span = Span(Position(5, 1, 54), Position(5, 12, 65)),
                    children = listOf(
                        Node.Leaf(
                            type = PrintlnKeywordNode,
                            value = StringValue("println"),
                            span = Span(Position(5, 1, 54), Position(5, 8, 61)),
                        ),
                        Node.Leaf(
                            type = LeftParenthesisNode,
                            value = StringValue("("),
                            span = Span(Position(5, 8, 61), Position(5, 9, 62)),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("y"),
                            span = Span(Position(5, 9, 62), Position(5, 10, 63)),
                        ),
                        Node.Leaf(
                            type = RightParenthesisNode,
                            value = StringValue(")"),
                            span = Span(Position(5, 10, 63), Position(5, 11, 64)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(5, 11, 64), Position(5, 12, 65)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "single-space-separation",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 16, 15)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(1, 1, 0), Position(1, 4, 3)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 4, 3), Position(1, 5, 4)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(1, 7, 6), Position(1, 13, 12)),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(1, 13, 12), Position(1, 14, 13)),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = IntegerValue(5),
                            span = Span(Position(1, 14, 13), Position(1, 15, 14)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(1, 15, 14), Position(1, 16, 15)),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 16), Position(2, 20, 35)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(2, 1, 16), Position(2, 4, 19)),
                            leading = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 16, 15), Position(2, 1, 16)))),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 4, 19), Position(2, 5, 20)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(2, 5, 20), Position(2, 6, 21)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(2, 6, 21), Position(2, 7, 22)),
                        ),
                        Node.Leaf(
                            type = StringTypeNode,
                            value = StringValue("string"),
                            span = Span(Position(2, 7, 22), Position(2, 13, 28)),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(2, 13, 28), Position(2, 14, 29)),
                        ),
                        Node.Leaf(
                            type = StringLiteralNode,
                            value = StringValue("Hello"),
                            span = Span(Position(2, 14, 29), Position(2, 21, 36)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(2, 19, 34), Position(2, 20, 35)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "space-around-operations",
            listOf(
                Node.Composite(
                    type = PrintlnStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 18, 17)),
                    children = listOf(
                        Node.Leaf(
                            type = PrintlnKeywordNode,
                            value = StringValue("println"),
                            span = Span(Position(1, 1, 0), Position(1, 8, 7)),
                        ),
                        Node.Leaf(
                            type = LeftParenthesisNode,
                            value = StringValue("("),
                            span = Span(Position(1, 8, 7), Position(1, 9, 8)),
                        ),
                        Node.Composite(
                            type = BinaryOperationExpressionNode,
                            span = Span(Position(1, 9, 8), Position(1, 16, 15)),
                            children = listOf(
                                Node.Composite(
                                    type = BinaryOperationExpressionNode,
                                    span = Span(Position(1, 9, 8), Position(1, 14, 13)),
                                    children = listOf(
                                        Node.Composite(
                                            type = BinaryOperationExpressionNode,
                                            span = Span(Position(1, 9, 8), Position(1, 12, 11)),
                                            children = listOf(
                                                Node.Leaf(
                                                    type = IdentifierNode,
                                                    value = StringValue("x"),
                                                    span = Span(Position(1, 9, 8), Position(1, 10, 9)),
                                                ),
                                                Node.Leaf(
                                                    type = MultiplyNode,
                                                    value = StringValue("*"),
                                                    span = Span(Position(1, 10, 9), Position(1, 11, 10)),
                                                ),
                                                Node.Leaf(
                                                    type = IdentifierNode,
                                                    value = StringValue("y"),
                                                    span = Span(Position(1, 11, 10), Position(1, 12, 11)),
                                                ),
                                            ),
                                        ),
                                        Node.Leaf(
                                            type = MinusNode,
                                            value = StringValue("-"),
                                            span = Span(Position(1, 12, 11), Position(1, 13, 12)),
                                        ),
                                        Node.Leaf(
                                            type = NumberLiteralNode,
                                            value = IntegerValue(4),
                                            span = Span(Position(1, 13, 12), Position(1, 14, 13)),
                                        ),
                                    ),
                                ),
                                Node.Leaf(
                                    type = PlusNode,
                                    value = StringValue("+"),
                                    span = Span(Position(1, 14, 13), Position(1, 15, 14)),
                                ),
                                Node.Leaf(
                                    type = NumberLiteralNode,
                                    value = IntegerValue(1),
                                    span = Span(Position(1, 15, 14), Position(1, 16, 15)),
                                ),
                            ),
                        ),
                        Node.Leaf(
                            type = RightParenthesisNode,
                            value = StringValue(")"),
                            span = Span(Position(1, 16, 15), Position(1, 17, 16)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(1, 17, 16), Position(1, 18, 17)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "spacing-after-colon",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 18, 17)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(1, 1, 0), Position(1, 4, 3)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 4, 3), Position(1, 5, 4)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(1, 7, 6), Position(1, 13, 12)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 13, 12), Position(1, 14, 13)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(1, 14, 13), Position(1, 15, 14)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 15, 14), Position(1, 16, 15)))),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = IntegerValue(5),
                            span = Span(Position(1, 16, 15), Position(1, 17, 16)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(1, 17, 16), Position(1, 18, 17)),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 18), Position(2, 20, 37)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(2, 1, 18), Position(2, 4, 21)),
                            leading = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 18, 17), Position(2, 1, 18)))),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 4, 21), Position(2, 5, 22)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("y"),
                            span = Span(Position(2, 5, 22), Position(2, 6, 23)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(2, 8, 25), Position(2, 9, 26)),
                            leading = listOf(Trivia(SpaceTrivia, "  ", Span(Position(2, 6, 23), Position(2, 8, 25)))),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(2, 9, 26), Position(2, 15, 32)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 15, 32), Position(2, 16, 33)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(2, 16, 33), Position(2, 17, 34)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 17, 34), Position(2, 18, 35)))),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = IntegerValue(6),
                            span = Span(Position(2, 18, 35), Position(2, 19, 36)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(2, 19, 36), Position(2, 20, 37)),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(3, 1, 38), Position(3, 20, 57)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(3, 1, 38), Position(3, 4, 41)),
                            leading = listOf(Trivia(NewlineTrivia, "\n", Span(Position(2, 20, 37), Position(3, 1, 38)))),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(3, 4, 41), Position(3, 5, 42)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("z"),
                            span = Span(Position(3, 5, 42), Position(3, 6, 43)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(3, 6, 43), Position(3, 7, 44)),
                            trailing = listOf(Trivia(SpaceTrivia, "  ", Span(Position(3, 7, 44), Position(3, 9, 46)))),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(3, 9, 46), Position(3, 15, 52)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(3, 15, 52), Position(3, 16, 53)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(3, 16, 53), Position(3, 17, 54)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(3, 17, 54), Position(3, 18, 55)))),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = IntegerValue(7),
                            span = Span(Position(3, 18, 55), Position(3, 19, 56)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(3, 19, 56), Position(3, 20, 57)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "spacing-around-equals",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 17, 16)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(1, 1, 0), Position(1, 4, 3)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 4, 3), Position(1, 5, 4)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 7, 6), Position(1, 8, 7)))),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(1, 8, 7), Position(1, 14, 13)),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(1, 14, 13), Position(1, 15, 14)),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = IntegerValue(5),
                            span = Span(Position(1, 15, 14), Position(1, 16, 15)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(1, 16, 15), Position(1, 17, 16)),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 17), Position(2, 28, 44)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(2, 1, 17), Position(2, 4, 20)),
                            leading = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 17, 16), Position(2, 1, 17)))),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 4, 20), Position(2, 5, 21)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("y"),
                            span = Span(Position(2, 5, 21), Position(2, 6, 22)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(2, 6, 22), Position(2, 7, 23)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 7, 23), Position(2, 8, 24)))),
                        ),
                        Node.Leaf(
                            type = StringTypeNode,
                            value = StringValue("string"),
                            span = Span(Position(2, 8, 24), Position(2, 14, 30)),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(2, 17, 33), Position(2, 18, 34)),
                            leading = listOf(Trivia(SpaceTrivia, "   ", Span(Position(2, 14, 30), Position(2, 17, 33)))),
                            trailing = listOf(Trivia(SpaceTrivia, "  ", Span(Position(2, 18, 34), Position(2, 20, 36)))),
                        ),
                        Node.Leaf(
                            type = StringLiteralNode,
                            value = StringValue("Hello"),
                            span = Span(Position(2, 20, 36), Position(2, 27, 43)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(2, 27, 43), Position(2, 28, 44)),
                        ),
                    ),
                ),

                Node.Composite(
                    type = AssignStatementNode,
                    span = Span(Position(3, 1, 45), Position(3, 5, 49)),
                    children = listOf(
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("z"),
                            span = Span(Position(3, 1, 45), Position(3, 2, 46)),
                            leading = listOf(Trivia(NewlineTrivia, "\n", Span(Position(2, 28, 44), Position(3, 1, 45)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(3, 2, 46), Position(3, 3, 47)),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = IntegerValue(6),
                            span = Span(Position(3, 3, 47), Position(3, 4, 48)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(3, 4, 48), Position(3, 5, 49)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "spacing-after-colon",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 18, 17)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(1, 1, 0), Position(1, 4, 3)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 4, 3), Position(1, 5, 4)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(1, 7, 6), Position(1, 13, 12)),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(1, 14, 13), Position(1, 15, 14)),
                            leading = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 13, 12), Position(1, 14, 13)))),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 15, 14), Position(1, 16, 15)))),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = StringValue("5"),
                            span = Span(Position(1, 16, 15), Position(1, 17, 16)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(1, 17, 16), Position(1, 18, 17)),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 18, 17), Position(2, 1, 18)))),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 18), Position(2, 20, 37)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(2, 1, 18), Position(2, 4, 21)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 4, 21), Position(2, 6, 23)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("y"),
                            span = Span(Position(2, 6, 23), Position(2, 7, 24)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(2, 8, 25), Position(2, 9, 26)),
                            leading = listOf(Trivia(SpaceTrivia, "  ", Span(Position(2, 7, 24), Position(2, 8, 25)))),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(2, 9, 26), Position(2, 15, 32)),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(2, 16, 33), Position(2, 17, 34)),
                            leading = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 15, 32), Position(2, 16, 33)))),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 17, 34), Position(2, 18, 35)))),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = StringValue("6"),
                            span = Span(Position(2, 18, 35), Position(2, 19, 36)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(2, 19, 36), Position(2, 20, 37)),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(2, 20, 37), Position(3, 1, 38)))),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(3, 1, 38), Position(3, 20, 57)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(3, 1, 38), Position(3, 4, 41)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(3, 4, 41), Position(3, 5, 42)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("z"),
                            span = Span(Position(3, 5, 42), Position(3, 6, 43)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(3, 6, 43), Position(3, 7, 44)),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(3, 9, 46), Position(3, 15, 52)),
                            leading = listOf(Trivia(SpaceTrivia, "  ", Span(Position(3, 7, 44), Position(3, 9, 46)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(3, 16, 53), Position(3, 17, 54)),
                            leading = listOf(Trivia(SpaceTrivia, " ", Span(Position(3, 15, 52), Position(3, 16, 53)))),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(3, 17, 54), Position(3, 18, 55)))),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = StringValue("7"),
                            span = Span(Position(3, 18, 55), Position(3, 19, 56)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(3, 19, 56), Position(3, 20, 57)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "spacing-around-equals",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 16, 15)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(1, 1, 0), Position(1, 4, 3)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 4, 3), Position(1, 5, 4)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(1, 8, 7), Position(1, 14, 13)),
                            leading = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 7, 6), Position(1, 8, 7)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(1, 14, 13), Position(1, 15, 14)),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = StringValue("5"),
                            span = Span(Position(1, 15, 14), Position(1, 16, 15)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(1, 16, 15), Position(1, 17, 16)),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 18), Position(2, 28, 44)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(2, 1, 18), Position(2, 4, 21)),
                            leading = listOf(Trivia(NewlineTrivia, "\n", Span(Position(2, 29, 45), Position(2, 30, 46)))),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 4, 21), Position(2, 5, 22)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("y"),
                            span = Span(Position(2, 5, 22), Position(2, 6, 23)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(2, 6, 23), Position(2, 7, 24)),
                        ),
                        Node.Leaf(
                            type = StringTypeNode,
                            value = StringValue("string"),
                            span = Span(Position(2, 8, 25), Position(2, 14, 31)),
                            leading = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 7, 24), Position(2, 8, 25)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(2, 17, 34), Position(2, 18, 35)),
                            leading = listOf(Trivia(SpaceTrivia, "   ", Span(Position(2, 14, 31), Position(2, 17, 34)))),
                            trailing = listOf(Trivia(SpaceTrivia, "  ", Span(Position(2, 18, 35), Position(2, 20, 37)))),
                        ),
                        Node.Leaf(
                            type = StringLiteralNode,
                            value = StringValue("\"Hello\""),
                            span = Span(Position(2, 20, 37), Position(2, 27, 44)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(2, 27, 44), Position(2, 28, 45)),
                        ),
                    ),
                ),

                Node.Composite(
                    type = AssignStatementNode,
                    span = Span(Position(3, 1, 46), Position(3, 5, 50)),
                    children = listOf(
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("z"),
                            span = Span(Position(3, 1, 46), Position(3, 2, 47)),
                            leading = listOf(Trivia(NewlineTrivia, "\n", Span(Position(2, 29, 45), Position(2, 30, 46)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(3, 2, 47), Position(3, 3, 48)),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = StringValue("6"),
                            span = Span(Position(3, 3, 48), Position(3, 4, 49)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(3, 4, 49), Position(3, 5, 50)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "spacing-before-colon",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 15, 14)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(1, 1, 0), Position(1, 4, 3)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 4, 3), Position(1, 5, 4)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(1, 7, 6), Position(1, 13, 12)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(1, 17, 16), Position(1, 18, 17)),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 18, 17), Position(2, 1, 18)))),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 18), Position(2, 21, 38)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(2, 1, 18), Position(2, 4, 21)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 4, 21), Position(2, 6, 23)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("y"),
                            span = Span(Position(2, 6, 23), Position(2, 7, 24)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(2, 8, 25), Position(2, 9, 26)),
                            leading = listOf(Trivia(SpaceTrivia, "  ", Span(Position(2, 7, 24), Position(2, 8, 25)))),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(2, 9, 26), Position(2, 15, 32)),
                            leading = listOf(Trivia(SpaceTrivia, "  ", Span(Position(2, 9, 26), Position(2, 9, 26)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(2, 16, 33), Position(2, 17, 34)),
                            leading = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 15, 32), Position(2, 16, 33)))),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 17, 34), Position(2, 18, 35)))),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = StringValue("6"),
                            span = Span(Position(2, 18, 35), Position(2, 19, 36)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(2, 19, 36), Position(2, 20, 37)),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(2, 20, 37), Position(3, 1, 38)))),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(3, 1, 38), Position(3, 15, 52)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(3, 1, 38), Position(3, 4, 41)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(3, 4, 41), Position(3, 5, 42)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("z"),
                            span = Span(Position(3, 5, 42), Position(3, 6, 43)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(3, 6, 43), Position(3, 7, 44)),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(3, 9, 46), Position(3, 15, 52)),
                            leading = listOf(Trivia(SpaceTrivia, "  ", Span(Position(3, 7, 44), Position(3, 9, 46)))),
                        ),
                        Node.Leaf(
                            type = AssignNode,
                            value = StringValue("="),
                            span = Span(Position(3, 16, 53), Position(3, 17, 54)),
                            leading = listOf(Trivia(SpaceTrivia, " ", Span(Position(3, 15, 52), Position(3, 16, 53)))),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(3, 17, 54), Position(3, 18, 55)))),
                        ),
                        Node.Leaf(
                            type = NumberLiteralNode,
                            value = StringValue("7"),
                            span = Span(Position(3, 18, 55), Position(3, 19, 56)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(3, 19, 56), Position(3, 20, 57)),
                        ),
                    ),
                ),
            ),
        )

        TestSnippetRegistry.register(
            "statement-linebreaks",
            listOf(
                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(1, 1, 0), Position(1, 13, 12)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(1, 1, 0), Position(1, 4, 3)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(1, 4, 3), Position(1, 5, 4)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("x"),
                            span = Span(Position(1, 5, 4), Position(1, 6, 5)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(1, 6, 5), Position(1, 7, 6)),
                        ),
                        Node.Leaf(
                            type = NumberTypeNode,
                            value = StringValue("number"),
                            span = Span(Position(1, 7, 6), Position(1, 13, 12)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(1, 13, 12), Position(1, 14, 13)),
                            trailing = listOf(Trivia(NewlineTrivia, "\n", Span(Position(1, 14, 13), Position(2, 1, 14)))),
                        ),
                    ),
                ),

                Node.Composite(
                    type = LetDeclarationStatementNode,
                    span = Span(Position(2, 1, 14), Position(2, 15, 28)),
                    children = listOf(
                        Node.Leaf(
                            type = LetNode,
                            value = StringValue("let"),
                            span = Span(Position(2, 1, 14), Position(2, 4, 17)),
                            trailing = listOf(Trivia(SpaceTrivia, " ", Span(Position(2, 4, 17), Position(2, 5, 18)))),
                        ),
                        Node.Leaf(
                            type = IdentifierNode,
                            value = StringValue("y"),
                            span = Span(Position(2, 5, 18), Position(2, 6, 19)),
                        ),
                        Node.Leaf(
                            type = ColonNode,
                            value = StringValue(":"),
                            span = Span(Position(2, 6, 19), Position(2, 7, 20)),
                        ),
                        Node.Leaf(
                            type = StringTypeNode,
                            value = StringValue("string"),
                            span = Span(Position(2, 7, 20), Position(2, 13, 26)),
                        ),
                        Node.Leaf(
                            type = SemicolonNode,
                            value = StringValue(";"),
                            span = Span(Position(2, 13, 26), Position(2, 14, 27)),
                        ),
                    ),
                ),
            ),
        )
    }
}

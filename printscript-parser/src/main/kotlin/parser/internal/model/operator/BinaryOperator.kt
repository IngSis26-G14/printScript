package parser.internal.model.operator

import common.model.node.NodeType

internal data class BinaryOperator(
    val nodeType: NodeType,
    val precedence: Int,
)

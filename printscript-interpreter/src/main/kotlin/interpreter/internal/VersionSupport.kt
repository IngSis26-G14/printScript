package interpreter.internal

import common.model.node.BlockNode
import common.model.node.BooleanLiteralNode
import common.model.node.BooleanTypeNode
import common.model.node.ConstDeclarationStatementNode
import common.model.node.IfStatementNode
import common.model.node.Node
import common.model.node.ReadEnvExpressionNode
import common.model.node.ReadInputExpressionNode

internal object VersionSupport {
    private val version11Types = setOf(
        BooleanLiteralNode,
        BooleanTypeNode,
        ConstDeclarationStatementNode,
        IfStatementNode,
        BlockNode,
        ReadInputExpressionNode,
        ReadEnvExpressionNode,
    )

    fun unsupportedNode(node: Node, version: String): Node? {
        if (version != "1.0") return null
        if (node.type in version11Types) return node
        if (node is Node.Composite) {
            for (child in node.children) {
                unsupportedNode(child, version)?.let { return it }
            }
        }
        return null
    }
}

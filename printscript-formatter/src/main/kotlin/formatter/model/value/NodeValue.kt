package formatter.model.value

import common.model.node.Node
import common.model.value.Value

internal data class NodeValue(val value: Node) : Value {
    override val type = NodeValueType
    override fun format(): String = value.toString()
}

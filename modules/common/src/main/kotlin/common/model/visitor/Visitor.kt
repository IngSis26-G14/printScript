package common.model.visitor

import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.model.value.Value
import common.type.outcome.Outcome

interface Visitor {
    fun visit(
        node: Node.Leaf,
        table: VisitorTable,
    ): Outcome<Value, Diagnostic>

    fun visit(
        node: Node.Composite,
        table: VisitorTable,
    ): Outcome<Value, Diagnostic>
}

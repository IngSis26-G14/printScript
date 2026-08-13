package common.model.visitor

import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.model.value.NoneValue
import common.model.value.Value
import common.type.outcome.Outcome

interface VisitorTable {
    val visitors: Collection<Visitor>

    fun dispatch(node: Node): Outcome<Value, Diagnostic> {
        var result: Outcome<Value, Diagnostic> = Outcome.Ok(NoneValue)

        for (visitor in visitors) {
            val visit = node.accept(visitor, this)

            if (visit is Outcome.Error) return visit

            if (result is Outcome.Ok && result.value is NoneValue && visit is Outcome.Ok) {
                val value = visit.value
                if (value !is NoneValue) {
                    result = visit
                }
            }
        }
        return result
    }
}

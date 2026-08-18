import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.type.outcome.Outcome

interface Validator {
    fun validate(
        version: String,
        nodes: Sequence<Node>,
    ): Sequence<Outcome<Node, Diagnostic>>
}

import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.model.token.Token
import common.type.outcome.Outcome

interface Parser {
    fun parse(
        version: String,
        tokens: Sequence<Token>,
    ): Sequence<Outcome<Node, Diagnostic>>
}

import common.model.diagnostic.Diagnostic
import common.model.node.Node
import common.model.rule.Rule

interface Linter {
    fun lint(
        version: String,
        nodes: Sequence<Node>,
        rules: Collection<Rule>,
    ): Sequence<Diagnostic>
}

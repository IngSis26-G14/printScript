import common.model.diagnostic.Diagnostic
import common.model.doc.Doc
import common.model.node.Node
import common.model.rule.Rule
import common.type.outcome.Outcome

interface Formatter {
    fun format(
        version: String,
        nodes: Sequence<Node>,
        rules: Collection<Rule>,
    ): Sequence<Outcome<Doc, Diagnostic>>
}

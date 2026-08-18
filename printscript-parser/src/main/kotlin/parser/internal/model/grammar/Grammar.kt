package parser.internal.model.grammar

import common.model.node.NodeType
import common.model.token.Token
import common.type.outcome.Outcome
import parser.internal.table.GrammarTable

internal interface Grammar {
    val type: NodeType

    fun match(
        tokens: List<Token>,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail>
}

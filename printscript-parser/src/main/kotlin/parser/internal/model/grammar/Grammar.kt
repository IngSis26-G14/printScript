package parser.internal.model.grammar

import common.model.node.NodeType
import common.type.outcome.Outcome
import parser.internal.buffer.TokenCursor
import parser.internal.table.GrammarTable

internal interface Grammar {
    val type: NodeType

    fun match(
        tokens: TokenCursor,
        table: GrammarTable,
    ): Outcome<GrammarMatch, GrammarFail>
}

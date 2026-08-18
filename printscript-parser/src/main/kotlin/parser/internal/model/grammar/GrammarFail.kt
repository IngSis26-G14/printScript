package parser.internal.model.grammar

import common.model.diagnostic.category.Category

internal data class GrammarFail(
    val message: String,
    val category: Category,
    val consumed: Int,
)

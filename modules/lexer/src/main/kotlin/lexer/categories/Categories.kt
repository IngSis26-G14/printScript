package lexer.categories

import common.model.diagnostic.category.Category

internal data object UnexpectedCharacter : Category {
    override val name = "UnexpectedCharacter"
}

internal data object Lexical : Category {
    override val name: String = "Lexical"
}
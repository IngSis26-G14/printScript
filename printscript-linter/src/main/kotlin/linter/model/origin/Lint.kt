package linter.model.origin

import common.model.diagnostic.category.Category

internal data object Lint : Category {
    override val name: String = "Lint"
}

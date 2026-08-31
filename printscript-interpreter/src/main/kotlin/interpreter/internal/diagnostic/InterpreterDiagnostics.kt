package interpreter.internal.diagnostic

import common.model.diagnostic.Diagnostic
import common.model.diagnostic.category.Category
import common.model.diagnostic.category.Configuration
import common.model.diagnostic.severity.Error

data object Semantic : Category {
    override val name: String = "Semantic"
}

data object Runtime : Category {
    override val name: String = "Runtime"
}

data class InterpreterDiagnostic(
    override val message: String,
    override val category: Category = Semantic,
) : Diagnostic {
    override val severity = Error
}

data class ConfigurationDiagnostic(
    override val message: String,
) : Diagnostic {
    override val severity = Error
    override val category = Configuration
}

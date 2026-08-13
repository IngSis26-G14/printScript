package lexer.error

import common.model.diagnostic.Diagnostic
import common.model.diagnostic.category.Category
import common.model.diagnostic.category.Configuration
import common.model.diagnostic.severity.Error
import common.model.diagnostic.severity.Severity

internal class ConfigurationError(
    override val message: String,
    override val severity: Severity = Error,
    override val category: Category = Configuration,
) : Diagnostic
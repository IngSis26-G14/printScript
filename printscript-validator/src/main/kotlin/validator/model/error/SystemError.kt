package validator.model.error

import common.model.diagnostic.Diagnostic
import common.model.diagnostic.category.Category
import common.model.diagnostic.severity.Error
import common.model.diagnostic.severity.Severity
import validator.model.category.System

internal class SystemError(
    override val message: String,
    override val severity: Severity = Error,
    override val category: Category = System,
) : Diagnostic

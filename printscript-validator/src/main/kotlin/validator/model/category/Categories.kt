package validator.model.category

import common.model.diagnostic.category.Category

internal data object IncorrectMethodCall : Category {
    override val name: String = "Incorrect Method Call"
}

internal data object InvalidReassignment : Category {
    override val name: String = "Invalid Reassignment"
}

internal data object InvalidValue : Category {
    override val name: String = "Invalid Value"
}

internal data object System : Category {
    override val name: String = "System"
}

internal data object InvalidType : Category {
    override val name: String = "InvalidType"
}

internal data object Redeclaration : Category {
    override val name: String = "Redeclaration"
}

internal data object TypeMismatch : Category {
    override val name: String = "Type Mismatch"
}

internal data object UndefinedIdentifier : Category {
    override val name: String = "Undefined Identifier"
}

internal data object UnsupportedOperator : Category {
    override val name: String = "Unsupported Operator"
}

internal data object MissingAssignment : Category {
    override val name: String = "Missing Assignment"
}

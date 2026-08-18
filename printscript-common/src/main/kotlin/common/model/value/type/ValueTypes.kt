package common.model.value.type

data object BooleanValueType : ValueType {
    override val name: String = "boolean"
}

data object NoneValueType : ValueType {
    override val name: String = "none"
}

data object NumberValueType : ValueType {
    override val name: String = "number"
}

data object StringValueType : ValueType {
    override val name: String = "string"
}

package common.model.rule

data class IntegerRuleValue(val value: Int) : RuleValue {
    override fun format() = value.toString()
    override fun type(): String = "integer"
}

data class BooleanRuleValue(val value: Boolean) : RuleValue {
    override fun format() = value.toString()
    override fun type(): String = "boolean"
}

data class StringRuleValue(val value: String) : RuleValue {
    override fun format() = value
    override fun type(): String = "string"
}

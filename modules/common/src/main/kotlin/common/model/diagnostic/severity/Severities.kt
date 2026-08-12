package common.model.diagnostic.severity

data object Info : Severity {
    override val name: String = "Info"
}

data object Warning : Severity {
    override val name: String = "Warning"
}

data object Error : Severity {
    override val name: String = "Error"
}

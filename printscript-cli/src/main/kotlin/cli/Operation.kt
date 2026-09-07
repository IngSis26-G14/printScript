package cli

enum class Operation {
    VALIDATION,
    EXECUTION,
    FORMATTING,
    ANALYZING;

    companion object {
        fun parse(value: String): Operation? =
            entries.firstOrNull {
                it.name.equals(value, ignoreCase = true)
            }
    }
}
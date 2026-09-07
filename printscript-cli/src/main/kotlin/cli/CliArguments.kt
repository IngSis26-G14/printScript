package cli

import java.nio.file.Path

data class CliArguments(
    val operation: Operation,
    val source: Path,
    val version: String = "1.0",
    val config: Path? = null,
)

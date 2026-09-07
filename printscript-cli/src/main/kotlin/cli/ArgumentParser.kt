package cli

import java.nio.file.Path

object ArgumentParser {
    private val supportedVersions = setOf("1.0", "1.1")

    fun parse(args: Array<String>): Result<CliArguments> {
        if (args.size < 2) {
            return failure(
                "Usage: printscript <operation> <source> " +
                        "[--version <1.0|1.1>] [--config <file>]",
            )
        }

        val operation = Operation.parse(args[0])
            ?: return failure(
                "Unknown operation '${args[0]}'. " +
                        "Expected validation, execution, formatting or analyzing.",
            )

        val source = Path.of(args[1])
        var version = "1.0"
        var config: Path? = null
        var index = 2

        while (index < args.size) {
            when (val option = args[index]) {
                "--version", "-v" -> {
                    val value = args.getOrNull(index + 1)
                        ?: return failure("Missing value for $option")

                    version = value
                    index += 2
                }

                "--config", "-c" -> {
                    val value = args.getOrNull(index + 1)
                        ?: return failure("Missing value for $option")

                    config = Path.of(value)
                    index += 2
                }

                else -> {
                    return failure("Unknown argument '$option'")
                }
            }
        }

        if (version !in supportedVersions) {
            return failure(
                "Unsupported PrintScript version '$version'. " +
                        "Supported versions: ${supportedVersions.joinToString()}",
            )
        }

        val requiresConfig =
            operation == Operation.FORMATTING ||
                    operation == Operation.ANALYZING

        if (requiresConfig && config == null) {
            return failure(
                "Operation '${operation.name.lowercase()}' requires --config.",
            )
        }

        return Result.success(
            CliArguments(
                operation = operation,
                source = source,
                version = version,
                config = config,
            ),
        )
    }

    private fun failure(message: String): Result<CliArguments> =
        Result.failure(IllegalArgumentException(message))
}
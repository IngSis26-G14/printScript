package cli

import java.nio.file.InvalidPathException
import java.nio.file.Path

object ArgumentParser {
    private val supportedVersions = setOf("1.0", "1.1")

    const val USAGE = """Usage: printscript <operation> <source> [options]

Operations:
  validation   Validate syntax and semantics
  execution    Validate and execute the source file
  formatting   Format the source file using a configuration file
  analyzing    Run the static code analyzer using a configuration file

Options:
  -v, --version <1.0|1.1>  PrintScript version (default: 1.0)
  -c, --config <file>      JSON configuration for formatting or analyzing
  -h, --help               Show this help message"""

    fun parse(args: Array<String>): Result<CliArguments> {
        if (args.size < 2) {
            return failure(USAGE)
        }

        val operation = Operation.parse(args[0])
            ?: return failure(
                "Unknown operation '${args[0]}'. " +
                    "Expected validation, execution, formatting or analyzing.",
            )

        val source = parsePath(args[1], "source").getOrElse { return Result.failure(it) }
        val options = parseOptions(args).getOrElse { return Result.failure(it) }

        if (options.version !in supportedVersions) {
            return failure(
                "Unsupported PrintScript version '${options.version}'. " +
                    "Supported versions: ${supportedVersions.joinToString()}",
            )
        }

        if (operation.requiresConfig() && options.config == null) {
            return failure(
                "Operation '${operation.name.lowercase()}' requires --config.",
            )
        }

        return Result.success(
            CliArguments(
                operation = operation,
                source = source,
                version = options.version,
                config = options.config,
            ),
        )
    }

    private fun parseOptions(args: Array<String>): Result<ParsedOptions> {
        var parsed = ParsedOptions()
        var index = 2

        while (index < args.size) {
            when (val option = args[index]) {
                "--version", "-v" -> {
                    val value = args.getOrNull(index + 1)
                        ?: return failure("Missing value for $option")

                    parsed = parsed.copy(version = value)
                    index += 2
                }

                "--config", "-c" -> {
                    val value = args.getOrNull(index + 1)
                        ?: return failure("Missing value for $option")

                    val path = parsePath(value, "configuration")
                        .getOrElse { return Result.failure(it) }
                    parsed = parsed.copy(config = path)
                    index += 2
                }

                else -> {
                    return failure("Unknown argument '$option'")
                }
            }
        }

        return Result.success(parsed)
    }

    private fun parsePath(value: String, description: String): Result<Path> = try {
        Result.success(Path.of(value))
    } catch (_: InvalidPathException) {
        Result.failure(IllegalArgumentException("Invalid $description path '$value'"))
    }

    private fun Operation.requiresConfig(): Boolean =
        this == Operation.FORMATTING || this == Operation.ANALYZING

    private fun <T> failure(message: String): Result<T> =
        Result.failure(IllegalArgumentException(message))

    private data class ParsedOptions(
        val version: String = "1.0",
        val config: Path? = null,
    )
}

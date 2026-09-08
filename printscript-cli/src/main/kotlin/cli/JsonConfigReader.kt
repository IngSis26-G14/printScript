package cli

import common.io.reader.config.ConfigReader
import common.model.diagnostic.Diagnostic
import common.model.diagnostic.category.Category
import common.model.diagnostic.category.Configuration
import common.model.diagnostic.severity.Error
import common.model.rule.BooleanRuleValue
import common.model.rule.IntegerRuleValue
import common.model.rule.Rule
import common.model.rule.RuleValue
import common.model.rule.StringRuleValue
import common.type.outcome.Outcome
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class JsonConfigReader(
    private val path: Path,
) : ConfigReader {
    override fun read(): Outcome<Collection<Rule>, Diagnostic> {
        val content = try {
            Files.readString(path)
        } catch (exception: IOException) {
            return Outcome.Error(
                ConfigurationDiagnostic(
                    "Could not read configuration '$path': ${exception.message}",
                ),
            )
        }

        return try {
            val values = FlatJsonObjectParser(content).parse()
            Outcome.Ok(values.map { (signature, value) -> Rule(signature, value) })
        } catch (exception: JsonConfigurationException) {
            Outcome.Error(ConfigurationDiagnostic(exception.message.orEmpty()))
        }
    }
}

private class FlatJsonObjectParser(
    private val source: String,
) {
    private var index = 0

    fun parse(): Map<String, RuleValue> {
        skipWhitespace()
        expect('{')
        skipWhitespace()

        val values = linkedMapOf<String, RuleValue>()
        if (consume('}')) {
            ensureEnd()
            return values
        }

        while (true) {
            val key = parseString()
            if (key in values) fail("Duplicate rule '$key'")
            skipWhitespace()
            expect(':')
            skipWhitespace()
            values[key] = parseValue()
            skipWhitespace()

            when {
                consume('}') -> break
                consume(',') -> skipWhitespace()
                else -> fail("Expected ',' or '}'")
            }
        }

        ensureEnd()
        return values
    }

    private fun parseValue(): RuleValue = when {
        peek() == '"' -> StringRuleValue(parseString())
        source.startsWith("true", index) -> {
            index += "true".length
            BooleanRuleValue(true)
        }
        source.startsWith("false", index) -> {
            index += "false".length
            BooleanRuleValue(false)
        }
        peek() == '-' || peek()?.isDigit() == true -> IntegerRuleValue(parseInteger())
        else -> fail("Expected a string, boolean, or integer rule value")
    }

    private fun parseInteger(): Int {
        val start = index
        consume('-')
        while (peek()?.isDigit() == true) index++
        return source.substring(start, index).toIntOrNull()
            ?: fail("Invalid integer value")
    }

    private fun parseString(): String {
        expect('"')
        val result = StringBuilder()

        while (index < source.length) {
            when (val character = source[index++]) {
                '"' -> return result.toString()
                '\\' -> result.append(parseEscape())
                else -> result.append(character)
            }
        }

        fail("Unterminated string")
    }

    private fun parseEscape(): Char {
        if (index >= source.length) fail("Unterminated escape sequence")
        return when (val escaped = source[index++]) {
            '"', '\\', '/' -> escaped
            'b' -> '\b'
            'f' -> '\u000C'
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            else -> fail("Unsupported escape sequence '\\$escaped'")
        }
    }

    private fun expect(expected: Char) {
        if (!consume(expected)) fail("Expected '$expected'")
    }

    private fun consume(expected: Char): Boolean {
        if (peek() != expected) return false
        index++
        return true
    }

    private fun peek(): Char? = source.getOrNull(index)

    private fun skipWhitespace() {
        while (peek()?.isWhitespace() == true) index++
    }

    private fun ensureEnd() {
        skipWhitespace()
        if (index != source.length) fail("Unexpected trailing content")
    }

    private fun fail(message: String): Nothing {
        throw JsonConfigurationException("$message at character ${index + 1} in '$sourceName'")
    }

    private val sourceName: String = "configuration"
}

private class JsonConfigurationException(message: String) : IllegalArgumentException(message)

private data class ConfigurationDiagnostic(
    override val message: String,
    override val category: Category = Configuration,
) : Diagnostic {
    override val severity = Error
}

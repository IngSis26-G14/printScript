package cli

import common.model.rule.BooleanRuleValue
import common.model.rule.IntegerRuleValue
import common.model.rule.StringRuleValue
import common.type.outcome.Outcome
import java.nio.file.Files
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class JsonConfigReaderTest {
    @Test
    fun `reads string boolean and integer rules`() {
        val config = createTempFile(suffix = ".json")
        Files.writeString(
            config,
            """
            {
              "identifier_format": "camel case",
              "mandatory-variable-or-literal-in-println": true,
              "line-breaks-before-println": 2
            }
            """.trimIndent(),
        )

        val outcome = assertIs<Outcome.Ok<*>>(JsonConfigReader(config).read())
        val rules = outcome.value as Collection<*>

        assertEquals(3, rules.size)
        val values = rules.filterIsInstance<common.model.rule.Rule>().associate { it.signature to it.value }
        assertEquals(StringRuleValue("camel case"), values["identifier_format"])
        assertEquals(BooleanRuleValue(true), values["mandatory-variable-or-literal-in-println"])
        assertEquals(IntegerRuleValue(2), values["line-breaks-before-println"])
    }

    @Test
    fun `reports malformed JSON as a configuration diagnostic`() {
        val config = createTempFile(suffix = ".json")
        Files.writeString(config, "{\"identifier_format\": }")

        val outcome = assertIs<Outcome.Error<*>>(JsonConfigReader(config).read())
        val diagnostic = assertIs<common.model.diagnostic.Diagnostic>(outcome.error)

        assertEquals("Configuration", diagnostic.category.name)
    }
}

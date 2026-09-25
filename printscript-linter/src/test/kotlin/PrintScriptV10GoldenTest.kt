import common.model.rule.BooleanRuleValue
import common.model.rule.IntegerRuleValue
import common.model.rule.Rule
import common.model.rule.StringRuleValue
import linter.PrintScriptLinter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import util.GoldenTester
import util.PrintScriptV10TestSnippets
import util.TestSnippetRegistry
import kotlin.io.path.Path

class PrintScriptV10GoldenTest {

    companion object {
        const val VERSION = "1.0"
        private val linter = PrintScriptLinter()

        @JvmStatic
        @BeforeAll
        fun setup() {
            PrintScriptV10TestSnippets
        }

        fun getSnippet(testName: String) = TestSnippetRegistry.get(testName).asSequence()
    }

    private fun run(
        testName: String,
        rules: Collection<Rule>,
    ) {
        val goldenPath = Path("src/test/resources/$VERSION/$testName/golden.ps")
        val nodes = getSnippet(testName)

        val results = linter.lint(
            VERSION,
            nodes,
            rules,
        )

        val actual = GoldenTester.format(results)
        val expected = GoldenTester.read(goldenPath)

        assertEquals(expected.trim(), actual.trim(), "FAILURE: $testName")
    }

    @Test fun `test case 1`() {
        val rules = listOf(Rule("mandatory-variable-or-literal-in-println", BooleanRuleValue(true)))
        run("expression-in-println", rules)
    }

    @Test fun `test case 2`() {
        val rules = listOf(Rule("identifier_format", StringRuleValue("camel case")))
        run("identifier-camel-case", rules)
    }

    @Test fun `test case 3`() {
        val rules = listOf(Rule("identifier_format", StringRuleValue("snake case")))
        run("identifier-snake-case", rules)
    }

    @Test fun `test case 4`() {
        val rules = listOf(Rule("identifier_format", IntegerRuleValue(5)))
        run("invalid-rule-value", rules)
    }

    @Test fun `test case 5`() {
        val rules = listOf(Rule("mandatory-variable-or-literal-in-print", BooleanRuleValue(true)))
        run("valid-println", rules)
    }
}

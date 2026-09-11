import common.model.rule.BooleanRuleValue
import common.model.rule.IntegerRuleValue
import common.model.rule.Rule
import formatter.PrintScriptFormatter
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
        private val formatter = PrintScriptFormatter()

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

        val results = formatter.format(
            VERSION,
            nodes,
            rules,
        )

        val actual = GoldenTester.format(results)
        val expected = GoldenTester.read(goldenPath)

        assertEquals(expected.trim(), actual.trim(), "FAILURE: $testName")
    }

    @Test fun `test case 1`() {
        val rules = listOf(Rule("enforce-no-spacing-around-equals", BooleanRuleValue(true)))
        run("no-spacing-around-equals", rules)
    }

    @Test fun `test case 2`() {
        val rules = listOf(Rule("line-breaks-after-println", IntegerRuleValue(2)))
        run("println-linebreaks", rules)
    }

    @Test fun `test case 3`() {
        val rules = listOf(Rule("mandatory-single-space-separation", BooleanRuleValue(true)))
        run("single-space-separation", rules)
    }

    @Test fun `test case 4`() {
        val rules = listOf(Rule("mandatory-space-surrounding-operations", BooleanRuleValue(true)))
        run("space-around-operations", rules)
    }

    @Test fun `test case 5`() {
        val rules = listOf(Rule("enforce-spacing-after-colon-in-declaration", BooleanRuleValue(true)))
        run("spacing-after-colon", rules)
    }

    @Test fun `test case 6`() {
        val rules = listOf(Rule("enforce-spacing-around-equals", BooleanRuleValue(true)))
        run("spacing-around-equals", rules)
    }

    @Test fun `test case 7`() {
        val rules = listOf(Rule("enforce-spacing-before-colon-in-declaration", BooleanRuleValue(true)))
        run("spacing-before-colon", rules)
    }

    @Test fun `test case 8`() {
        val rules = listOf(Rule("mandatory-line-break-after-statement", BooleanRuleValue(true)))
        run("statement-linebreaks", rules)
    }
}

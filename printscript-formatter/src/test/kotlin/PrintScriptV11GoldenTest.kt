import common.model.rule.BooleanRuleValue
import common.model.rule.IntegerRuleValue
import common.model.rule.Rule
import formatter.PrintScriptFormatter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import util.GoldenTester
import util.PrintScriptV11TestSnippets
import util.TestSnippetRegistry
import kotlin.io.path.Path

class PrintScriptV11GoldenTest {

    companion object {
        const val VERSION = "1.1"
        private val formatter = PrintScriptFormatter()

        @JvmStatic
        @BeforeAll
        fun setup() {
            PrintScriptV11TestSnippets
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

    @Test
    fun `test case 1`() {
        val rules = listOf(
            Rule("if-brace-below-line", BooleanRuleValue(true)),
        )
        run(
            "if-brace-below-line",
            rules,
        )
    }

    @Test
    fun `test case 2`() {
        val rules = listOf(
            Rule("if-brace-same-line", BooleanRuleValue(true)),
        )
        run(
            "if-brace-same-line",
            rules,
        )
    }

    @Test
    fun `test case 3`() {
        val rules = listOf(
            Rule("indent-inside-if", IntegerRuleValue(4)),
        )
        run(
            "indents-in-if",
            rules,
        )
    }
}

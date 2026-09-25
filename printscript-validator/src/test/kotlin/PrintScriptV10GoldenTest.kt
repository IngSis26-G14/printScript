import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import util.GoldenTester
import util.PrintScriptV10TestSnippets
import util.TestSnippetRegistry
import validator.PrintScriptValidator
import kotlin.io.path.Path

class PrintScriptV10GoldenTest {
    companion object {
        const val VERSION = "1.0"

        @JvmStatic
        @BeforeAll
        fun setup() {
            PrintScriptV10TestSnippets
        }
    }

    private fun run(testName: String) {
        val validator = PrintScriptValidator()
        val goldenPath = Path("src/test/resources/$VERSION/$testName/golden.ps")

        val nodes = TestSnippetRegistry.get(testName).asSequence()

        val results = validator.validate(VERSION, nodes)
        val actual = GoldenTester.format(results)
        val expected = GoldenTester.read(goldenPath)

        assertEquals(expected.trim(), actual.trim(), "FAILURE: $testName")
    }

    @Test
    fun `test case 1`() {
        run("empty-nodes")
    }

    @Test
    fun `test case 2`() {
        run("identifier-redeclaration")
    }

    @Test
    fun `test case 3`() {
        run("invalid-string-binary-operation")
    }

    @Test
    fun `test case 4`() {
        run("invalid-string-unary-operation")
    }

    @Test
    fun `test case 5`() {
        run("invalid-type-assignation")
    }

    @Test
    fun `test case 6`() {
        run("none-value")
    }

    @Test
    fun `test case 7`() {
        run("undefined-identifier")
    }

    @Test
    fun `test case 8`() {
        run("valid-statements")
    }
}

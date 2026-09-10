import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import parser.PrintScriptParser
import util.GoldenTester
import util.PrintScriptV10TestSnippets
import util.TestSnippetRegistry
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
        val parser = PrintScriptParser()
        val goldenPath = Path("src/test/resources/$VERSION/$testName/golden.ps")

        val tokens = TestSnippetRegistry.get(testName).asSequence()

        val results = parser.parse(VERSION, tokens)
        val actual = GoldenTester.format(results)
        val expected = GoldenTester.read(goldenPath)

        assertEquals(expected.trim(), actual.trim(), "FAILURE: $testName")
    }

    @Test
    fun `test case 1`() {
        run("empty-println-statement")
    }

    @Test
    fun `test case 2`() {
        run("expression-as-statement")
    }

    @Test
    fun `test case 3`() {
        run("identifier-and-number-binary-expression")
    }

    @Test
    fun `test case 4`() {
        run("identifier-and-string-binary-expression")
    }

    @Test
    fun `test case 5`() {
        run("let-assignation-statement")
    }

    @Test
    fun `test case 6`() {
        run("let-declaration-statement")
    }

    @Test
    fun `test case 7`() {
        run("missing-end-of-statement")
    }

    @Test
    fun `test case 8`() {
        run("println-statement")
    }

    @Test
    fun `test case 9`() {
        run("println-with-binary-operation")
    }

    @Test
    fun `test case 11`() {
        run("string-and-number-binary-expression")
    }
}

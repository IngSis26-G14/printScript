import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import parser.PrintScriptParser
import util.GoldenTester
import util.PrintScriptV11TestSnippets
import util.TestSnippetRegistry
import kotlin.io.path.Path

class PrintScriptV11GoldenTest {
    companion object {
        const val VERSION = "1.1"

        @JvmStatic
        @BeforeAll
        fun setup() {
            PrintScriptV11TestSnippets
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
        run("const-declaration-statement")
    }

    @Test
    fun `test case 2`() {
        run("empty-readEnv-expression")
    }

    @Test
    fun `test case 3`() {
        run("empty-readInput-expression")
    }

    @Test
    fun `test case 4`() {
        run("if-else-statement")
    }

    @Test
    fun `test case 5`() {
        run("if-statement")
    }

    @Test
    fun `test case 6`() {
        run("nested-if-else-statement")
    }

    @Test
    fun `test case 7`() {
        run("readEnv-expression")
    }

    @Test
    fun `test case 8`() {
        run("readInput-expression")
    }
}

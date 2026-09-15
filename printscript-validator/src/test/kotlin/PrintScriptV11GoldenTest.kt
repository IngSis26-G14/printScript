import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import util.GoldenTester
import util.PrintScriptV11TestSnippets
import util.TestSnippetRegistry
import validator.PrintScriptValidator
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
        run("const-invalid-assignment")
    }

    @Test
    fun `test case 2`() {
        run("const-missing-assignment")
    }

    @Test
    fun `test case 3`() {
        run("const-reassignment")
    }

    @Test
    fun `test case 4`() {
        run("const-with-runtime-values")
    }

    @Test
    fun `test case 5`() {
        run("invalid-boolean-binary-operation")
    }

    @Test
    fun `test case 6`() {
        run("invalid-type-readEnv-argument")
    }

    @Test
    fun `test case 7`() {
        run("invalid-type-readInput-argument")
    }

    @Test
    fun `test case 8`() {
        run("invalid-type-unary-operation")
    }

    @Test
    fun `test case 9`() {
        run("missing-readEnv-argument")
    }

    @Test
    fun `test case 10`() {
        run("missing-readInput-argument")
    }

    @Test
    fun `test case 11`() {
        run("valid-statements")
    }
}

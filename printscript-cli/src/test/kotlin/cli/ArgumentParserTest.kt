package cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArgumentParserTest {
    @Test
    fun `uses version 1_0 by default`() {
        val arguments = ArgumentParser.parse(
            arrayOf("validation", "program.ps"),
        ).getOrThrow()

        assertEquals(Operation.VALIDATION, arguments.operation)
        assertEquals("1.0", arguments.version)
    }

    @Test
    fun `accepts version 1_1 from the terminal options`() {
        val arguments = ArgumentParser.parse(
            arrayOf("execution", "program.ps", "--version", "1.1"),
        ).getOrThrow()

        assertEquals(Operation.EXECUTION, arguments.operation)
        assertEquals("1.1", arguments.version)
    }

    @Test
    fun `rejects unsupported versions`() {
        val result = ArgumentParser.parse(
            arrayOf("validation", "program.ps", "-v", "2.0"),
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("Unsupported"))
    }

    @Test
    fun `requires configuration for analyzing`() {
        val result = ArgumentParser.parse(
            arrayOf("analyzing", "program.ps"),
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("requires --config"))
    }
}

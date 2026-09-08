package cli

import common.io.reader.env.EnvReader
import common.io.reader.input.InputReader
import common.io.writer.OutputWriter
import common.model.value.StringValue
import common.model.value.Value
import common.type.option.Option
import java.io.BufferedReader
import java.io.PrintStream

class StandardInputReader(
    private val input: BufferedReader,
) : InputReader {
    override fun read(): Sequence<Char> =
        input.readLine()?.asSequence() ?: emptySequence()
}

class ConsoleOutputWriter(
    private val output: PrintStream,
) : OutputWriter {
    override fun write(input: Sequence<String>) {
        input.forEach(output::println)
    }
}

class SystemEnvReader(
    private val environment: Map<String, String> = System.getenv(),
) : EnvReader {
    override fun read(key: String): Option<Value> =
        environment[key]
            ?.let { Option.Some(StringValue(it)) }
            ?: Option.None
}

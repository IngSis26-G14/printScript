import common.io.reader.env.EnvReader
import common.io.reader.input.InputReader
import common.io.writer.OutputWriter
import common.model.diagnostic.Diagnostic
import common.model.node.Node

interface Interpreter {
    fun interpret(
        version: String,
        nodes: Sequence<Node>,
        input: InputReader,
        output: OutputWriter,
        env: EnvReader,
    ): Sequence<Diagnostic>
}

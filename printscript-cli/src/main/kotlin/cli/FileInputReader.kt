package cli

import common.io.reader.input.InputReader
import java.nio.file.Files
import java.nio.file.Path

class FileInputReader(
    private val path: Path,
) : InputReader {
    override fun read(): Sequence<Char> =
        Files.readString(path).asSequence()
}
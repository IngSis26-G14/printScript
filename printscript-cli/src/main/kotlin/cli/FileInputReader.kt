package cli

import common.io.reader.input.InputReader
import java.nio.file.Files
import java.nio.file.Path

class FileInputReader(
    private val path: Path,
) : InputReader {
    override fun read(): Sequence<Char> = sequence {
        Files.newBufferedReader(path).use { reader ->
            val buffer = CharArray(DEFAULT_BUFFER_SIZE)
            var count = reader.read(buffer)

            while (count >= 0) {
                for (index in 0 until count) {
                    yield(buffer[index])
                }
                count = reader.read(buffer)
            }
        }
    }.constrainOnce()

    fun characterCount(): Long {
        var total = 0L
        Files.newBufferedReader(path).use { reader ->
            val buffer = CharArray(DEFAULT_BUFFER_SIZE)
            var count = reader.read(buffer)

            while (count >= 0) {
                total += count
                count = reader.read(buffer)
            }
        }
        return total
    }
}

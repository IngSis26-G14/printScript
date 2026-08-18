package common.io.reader.env

import common.model.value.Value
import common.type.option.Option

interface EnvReader {
    fun read(key: String): Option<Value>
}

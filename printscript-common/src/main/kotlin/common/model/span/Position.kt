package common.model.span

data class Position(val line: Int, val column: Int, val index: Int) {
    fun format(): String = "$line:$column"
}

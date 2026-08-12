package common.model.span

data class Span(val start: Position, val end: Position) {
    fun format(): String = "${start.format()}-${end.format()}"
}

package common.type.outcome

sealed interface Outcome<out T, out E> {
    data class Ok<out T>(val value: T) : Outcome<T, Nothing>
    data class Error<out E>(val error: E) : Outcome<Nothing, E>
}

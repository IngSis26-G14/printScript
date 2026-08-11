package common.type.outcome

inline fun <T, E> Outcome<T, E>.getOrElse(default: (E) -> T): T {
    return when (this) {
        is Outcome.Ok -> this.value
        is Outcome.Error -> default(this.error)
    }
}

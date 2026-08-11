package common.type.option

inline fun <T> Option<T>.maxByOrEqual(compare: (T) -> Int, candidate: T): Option<T> = when (this) {
    is Option.None -> Option.Some(candidate)
    is Option.Some -> if (compare(candidate) >= compare(this.value)) Option.Some(candidate) else this
}

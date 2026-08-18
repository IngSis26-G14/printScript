package common.type.option

inline fun <T, R> Option<T>.map(transform: (T) -> R): Option<R> = when (this) {
    is Option.Some -> Option.Some(transform(this.value))
    is Option.None -> Option.None
}

package common.type.option

inline fun <T> Option<T>.getOrElse(default: () -> T): T = when (this) {
    is Option.Some -> this.value
    is Option.None -> default()
}

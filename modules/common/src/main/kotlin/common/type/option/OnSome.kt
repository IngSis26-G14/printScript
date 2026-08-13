package common.type.option

inline fun <T> Option<T>.onSome(action: (T) -> Unit) {
    if (this is Option.Some) action(value)
}

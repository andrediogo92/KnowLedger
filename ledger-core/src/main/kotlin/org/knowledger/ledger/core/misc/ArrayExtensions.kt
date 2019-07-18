package org.knowledger.ledger.core.misc

inline fun <T, reified R> Array<out T>.mapToArray(
    transform: (T) -> R
): Array<R> =
    Array(size) {
        transform(this[it])
    }

inline fun <T, reified R> Array<out T>.mapAndAdd(
    transform: (T) -> R, vararg toAdd: T
): Array<R> =
    Array(size + toAdd.size) {
        when (it) {
            in 0 until toAdd.size -> transform(toAdd[it])
            else -> transform(this[it - toAdd.size])
        }
    }
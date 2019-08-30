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
            in toAdd.indices -> transform(toAdd[it])
            else -> transform(this[it - toAdd.size])
        }
    }

fun <T> List<T>.filterByIndex(
    function: (Int) -> Boolean
): List<T> {
    val mutList = mutableListOf<T>()
    var i = 0
    while (i < size) {
        if (function(i)) {
            mutList += this[i]
        }
        i++
    }
    return mutList
}

fun <T> Iterable<T>.filterByIndex(
    function: (Int) -> Boolean
): Iterable<T> =
    iterator().filterByIndex(function)

fun <T> Iterator<T>.filterByIndex(
    function: (Int) -> Boolean
): List<T> {
    val mutList = mutableListOf<T>()
    var i = 0
    while (hasNext()) {
        if (function(i)) {
            mutList += next()
        }
        i++
    }
    return mutList
}

fun <T> Sequence<T>.filterByIndex(
    function: (Int) -> Boolean
): List<T> =
    iterator().filterByIndex(function)

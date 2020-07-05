package org.knowledger.collections

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T, U, V> Pair<T, U>.mapSecond(
    map: (U) -> V
): Pair<T, V> {
    contract {
        callsInPlace(map, InvocationKind.EXACTLY_ONCE)
    }
    return Pair(first, map(second))
}

inline fun <T, U, V> Pair<T, U>.mapFirst(
    map: (T) -> V
): Pair<V, U> {
    contract {
        callsInPlace(map, InvocationKind.EXACTLY_ONCE)
    }
    return Pair(map(first), second)
}

inline fun <T, U, V, W> Pair<T, U>.map(
    left: (T) -> V,
    right: (U) -> W
): Pair<V, W> {
    contract {
        callsInPlace(left, InvocationKind.EXACTLY_ONCE)
        callsInPlace(right, InvocationKind.EXACTLY_ONCE)
    }
    return Pair(left(first), right(second))
}

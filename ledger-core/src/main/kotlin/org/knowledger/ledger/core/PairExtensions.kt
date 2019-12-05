package org.knowledger.ledger.core

inline fun <T, U, V> Pair<T, U>.mapSecond(
    map: (U) -> V
): Pair<T, V> =
    Pair(first, map(second))

inline fun <T, U, V> Pair<T, U>.mapFirst(
    map: (T) -> V
): Pair<V, U> =
    Pair(map(first), second)

inline fun <T, U, V, W> Pair<T, U>.map(
    left: (T) -> V,
    right: (U) -> W
): Pair<V, W> =
    Pair(left(first), right(second))

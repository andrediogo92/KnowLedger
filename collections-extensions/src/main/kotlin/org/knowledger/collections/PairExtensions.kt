package org.knowledger.collections

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T, U, V> Pair<T, U>.mapSecond(map: (U) -> V): Pair<T, V> {
    contract {
        callsInPlace(map, InvocationKind.EXACTLY_ONCE)
    }
    return Pair(first, map(second))
}

inline fun <T, U, V> Pair<T, U>.mapFirst(map: (T) -> V): Pair<V, U> {
    contract {
        callsInPlace(map, InvocationKind.EXACTLY_ONCE)
    }
    return Pair(map(first), second)
}


inline fun <T, U, V, W> Pair<T, U>.map(left: (T) -> V, right: (U) -> W): Pair<V, W> {
    contract {
        callsInPlace(left, InvocationKind.EXACTLY_ONCE)
        callsInPlace(right, InvocationKind.EXACTLY_ONCE)
    }
    return Pair(left(first), right(second))
}

inline fun <S : T, T, U> Pair<T, U>.foldFirst(zero: S, reducer: (S, T) -> S): Pair<S, U> {
    contract {
        callsInPlace(reducer, InvocationKind.EXACTLY_ONCE)
    }
    return Pair(reducer(zero, first), second)
}

inline fun <S : T, T, U> Pair<U, T>.foldSecond(zero: S, reducer: (S, T) -> S): Pair<U, S> {
    contract {
        callsInPlace(reducer, InvocationKind.EXACTLY_ONCE)
    }
    return Pair(first, reducer(zero, second))
}

inline fun <S : T, T> Pair<T, T>.fold(zero: S, reducer: (S, T) -> S): Pair<S, S> {
    contract {
        callsInPlace(reducer, InvocationKind.AT_LEAST_ONCE)
    }
    return Pair(reducer(zero, first), reducer(zero, second))
}

inline fun <S : T, T, U : V, V> Pair<T, U>.fold(
    zeroFirst: S, reducerFirst: (S, T) -> S, zeroSecond: U, reducerSecond: (U, V) -> U,
): Pair<S, V> {
    contract {
        callsInPlace(reducerFirst, InvocationKind.EXACTLY_ONCE)
        callsInPlace(reducerSecond, InvocationKind.EXACTLY_ONCE)
    }
    return Pair(reducerFirst(zeroFirst, first), reducerSecond(zeroSecond, second))
}

inline fun <S : T, T, L : Iterable<T>, U> Pair<L, U>.foldIterableFirst(
    zero: S, reducer: (S, T) -> S,
): Pair<S, U> {
    contract {
        callsInPlace(reducer, InvocationKind.UNKNOWN)
    }
    return Pair(first.fold(zero, reducer), second)
}

inline fun <S : T, T, L : Iterable<T>, U> Pair<U, L>.foldIterableSecond(
    zero: S, reducer: (S, T) -> S,
): Pair<U, S> {
    contract {
        callsInPlace(reducer, InvocationKind.UNKNOWN)
    }
    return Pair(first, second.fold(zero, reducer))
}

inline fun <S : T, T, L : Iterable<T>> Pair<L, L>.foldIterables(
    zero: S, reducer: (S, T) -> S,
): Pair<S, S> {
    contract {
        callsInPlace(reducer, InvocationKind.UNKNOWN)
    }
    return Pair(first.fold(zero, reducer), second.fold(zero, reducer))
}

inline fun <S : T, T, L : Iterable<T>, U : V, V, W : Iterable<V>> Pair<L, W>.foldIterables(
    zeroFirst: S, reducerFirst: (S, T) -> S, zeroSecond: U, reducerSecond: (U, V) -> U,
): Pair<S, U> {
    contract {
        callsInPlace(reducerFirst, InvocationKind.UNKNOWN)
        callsInPlace(reducerSecond, InvocationKind.UNKNOWN)
    }
    return Pair(
        first.fold(zeroFirst, reducerFirst),
        second.fold(zeroSecond, reducerSecond)
    )
}


inline fun <T, L : Iterable<T>, U> Pair<L, U>.forEachFirst(action: (T) -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.UNKNOWN)
    }
    first.forEach(action)
}

inline fun <T, L : Iterable<T>, U> Pair<U, L>.forEachSecond(action: (T) -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.UNKNOWN)
    }
    second.forEach(action)
}

inline fun <T, L : Iterable<T>> Pair<L, L>.forEach(action: (T) -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.UNKNOWN)
    }
    first.forEach(action)
    second.forEach(action)
}

inline fun <T, L : Iterable<T>, U, V : Iterable<U>> Pair<L, V>.forEach(
    actionFirst: (T) -> Unit, actionSecond: (U) -> Unit,
) {
    contract {
        callsInPlace(actionFirst, InvocationKind.UNKNOWN)
        callsInPlace(actionSecond, InvocationKind.UNKNOWN)
    }
    first.forEach(actionFirst)
    second.forEach(actionSecond)
}

inline fun <T, L : Iterable<T>, U> Pair<L, U>.onEachFirst(action: (T) -> Unit): Pair<L, U> {
    contract {
        callsInPlace(action, InvocationKind.UNKNOWN)
    }
    return apply { first.forEach(action) }
}

inline fun <T, L : Iterable<T>, U> Pair<U, L>.onEachSecond(action: (T) -> Unit): Pair<U, L> {
    contract {
        callsInPlace(action, InvocationKind.UNKNOWN)
    }
    return apply { second.forEach(action) }
}

inline fun <T, L : Iterable<T>> Pair<L, L>.onEach(action: (T) -> Unit): Pair<L, L> {
    contract {
        callsInPlace(action, InvocationKind.UNKNOWN)
    }
    return apply {
        first.forEach(action)
        second.forEach(action)
    }
}

inline fun <T, L : Iterable<T>, U, V : Iterable<U>> Pair<L, V>.onEach(
    actionFirst: (T) -> Unit, actionSecond: (U) -> Unit,
): Pair<L, V> {
    contract {
        callsInPlace(actionFirst, InvocationKind.UNKNOWN)
        callsInPlace(actionSecond, InvocationKind.UNKNOWN)
    }
    return apply {
        first.forEach(actionFirst)
        second.forEach(actionSecond)
    }
}
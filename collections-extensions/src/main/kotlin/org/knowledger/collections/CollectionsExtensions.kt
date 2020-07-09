package org.knowledger.collections

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <T : Comparable<T>> emptySortedList(): SortedList<T> =
    DelegatedSortedList()

fun <T : Comparable<T>> mutableSortedListOf(
    vararg elements: T
): MutableSortedList<T> =
    elements.asIterable().toMutableSortedList()

fun <T : Comparable<T>> sortedListOf(
    vararg elements: T
): SortedList<T> =
    elements.asIterable().toSortedList()

fun <T : Comparable<T>> Sequence<T>.toMutableSortedListFromPreSorted(): MutableSortedList<T> =
    asIterable().toMutableSortedListFromPreSorted()

fun <T : Comparable<T>> Iterable<T>.toMutableSortedListFromPreSorted(): MutableSortedList<T> =
    DelegatedSortedList(delegate = toMutableList())

fun <T : Comparable<T>> List<T>.toMutableSortedListFromPreSorted(): MutableSortedList<T> =
    asIterable().toMutableSortedListFromPreSorted()


fun <T : Comparable<T>> Array<T>.toMutableSortedList(): MutableSortedList<T> =
    asIterable().toMutableSortedList()

fun <T : Comparable<T>> Sequence<T>.toMutableSortedList(): MutableSortedList<T> =
    asIterable().toMutableSortedList()

fun <T : Comparable<T>> Iterable<T>.toMutableSortedList(): MutableSortedList<T> =
    DelegatedSortedList(initial = this)


fun <T : Comparable<T>> Sequence<T>.toSortedListFromPreSorted(): SortedList<T> =
    asIterable().toSortedListFromPreSorted()

fun <T : Comparable<T>> Iterable<T>.toSortedListFromPreSorted(): SortedList<T> =
    DelegatedSortedList(delegate = toMutableList())

fun <T : Comparable<T>> List<T>.toSortedListFromPreSorted(): SortedList<T> =
    asIterable().toSortedListFromPreSorted()


fun <T : Comparable<T>> Array<T>.toSortedList(): SortedList<T> =
    asIterable().toSortedList()

fun <T : Comparable<T>> Sequence<T>.toSortedList(): SortedList<T> =
    asIterable().toSortedList()

fun <T : Comparable<T>> Iterable<T>.toSortedList(): SortedList<T> =
    DelegatedSortedList(initial = this)

/**
 * Similar to [kotlin.collections.slice] but unsafe.
 * Builds an exactly sized [ArrayList] in place with elements from [from] index
 * up until [toExclusive] index.
 * Does no bounds checking whatsoever.
 */
fun <T> List<T>.fastSlice(from: Int, toExclusive: Int): List<T> =
    ArrayList<T>(toExclusive - from).apply {
        for (i in from until toExclusive) {
            add(this@fastSlice[i])
        }
    }

/**
 * Similar to [kotlin.collections.slice] but unsafe.
 * Builds an exactly sized [Array] in place with elements from [from] index
 * up until [toExclusive] index.
 * Does no bounds checking whatsoever.
 */
inline fun <reified T> Array<T>.fastSlice(from: Int, toExclusive: Int): Array<T> =
    Array(toExclusive - from) {
        this@fastSlice[it + from]
    }


inline fun <T, R : Comparable<R>> Iterable<T>.mapSorted(map: (T) -> R): SortedList<R> =
    DelegatedSortedList(map(map))

inline fun <reified T> Sequence<T>.toSizedArray(i: Int): Array<T> {
    val iter = iterator()
    return Array(i) {
        iter.next()
    }
}

inline fun <reified T> Iterable<T>.toSizedArray(i: Int): Array<T> {
    val iter = iterator()
    return Array(i) {
        iter.next()
    }
}

inline fun <T, reified R> List<T>.mapToArray(
    transform: (T) -> R
): Array<R> =
    Array(size) {
        transform(this[it])
    }

inline fun <reified T> Array<out T>.fastPrefixAdd(prefix: T): Array<T> {
    val result = Array(size + 1) {
        this[(it + size - 1) % size]
    }
    result[0] = prefix
    return result
}

inline fun <T, reified R> Array<out T>.mapToArray(
    transform: (T) -> R
): Array<R> =
    Array(size) {
        transform(this[it])
    }

inline fun <T, reified R> Array<out T>.mapAndPrefixAdd(
    transform: (T) -> R, toAdd: T
): Array<R> {
    contract {
        callsInPlace(transform, kind = InvocationKind.AT_LEAST_ONCE)
    }
    val result = Array(size + 1) {
        transform(this[(it + size - 1) % size])
    }
    result[0] = transform(toAdd)
    return result
}

inline fun <T, reified R> Array<out T>.mapAndSuffixAdd(
    transform: (T) -> R, toAdd: T
): Array<R> {
    contract {
        callsInPlace(transform, kind = InvocationKind.AT_LEAST_ONCE)
    }
    val result = Array(size + 1) {
        transform(this[it % size])
    }
    result[size] = transform(toAdd)
    return result
}


fun <T> List<T>.filterByIndex(
    function: (Int) -> Boolean
): List<T> {
    val mutList = ArrayList<T>(size / 2)
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
): List<T> =
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

fun <E> MutableIterable<E>.removeByUnique(
    predicate: (E) -> Boolean
): Boolean {
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.next())) {
            it.remove()
            return true
        }
    }
    return false
}


fun <T> Sequence<T>.filterByIndex(
    function: (Int) -> Boolean
): List<T> =
    iterator().filterByIndex(function)


inline fun <T, R> Collection<T>.mapMutableList(
    map: (T) -> R
): MutableList<R> =
    ArrayList<R>(size).also { al ->
        forEach {
            al += map(it)
        }
    }

inline fun <T, R> Collection<T>.mapMutableSet(
    map: (T) -> R
): MutableSet<R> =
    LinkedHashSet<R>(size).also { lhs ->
        forEach {
            lhs += map(it)
        }
    }

inline fun <T, R, S> Map<T, R>.mapMutable(
    map: (R) -> S
): MutableMap<T, S> =
    LinkedHashMap<T, S>(size).also { lhm ->
        forEach {
            lhm[it.key] = map(it.value)
        }
    }


inline fun <T, R> Collection<T>.mapToSet(
    map: (T) -> R
): Set<R> =
    LinkedHashSet<R>(size).also { lhs ->
        forEach {
            lhs += map(it)
        }
    }

inline fun <T, R> Collection<T>.mapToSortedSet(
    map: (T) -> R
): SortedSet<R> =
    mapTo(TreeSet()) {
        map(it)
    }


inline fun <T> Collection<T>.copy(
    clone: (T) -> T
): List<T> =
    map(clone)

inline fun <T> Collection<T>.copyMutableList(
    clone: (T) -> T
): MutableList<T> =
    mapMutableList(clone)

inline fun <T> Collection<T>.copySet(
    clone: (T) -> T
): Set<T> =
    mapToSet(clone)

inline fun <T> Collection<T>.copyMutableSet(
    clone: (T) -> T
): MutableSet<T> =
    mapMutableSet(clone)

inline fun <T> Collection<T>.copySortedSet(
    clone: (T) -> T
): SortedSet<T> =
    mapToSortedSet(clone)

inline fun <T : Comparable<T>> Collection<T>.copySortedList(
    clone: (T) -> T
): SortedList<T> =
    map(clone).toSortedListFromPreSorted()

inline fun <T : Comparable<T>> Collection<T>.copyMutableSortedList(
    clone: (T) -> T
): MutableSortedList<T> =
    map(clone).toMutableSortedListFromPreSorted()

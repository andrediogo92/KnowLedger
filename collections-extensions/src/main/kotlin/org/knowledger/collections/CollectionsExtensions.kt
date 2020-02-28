package org.knowledger.collections

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

fun <T : Comparable<T>> sortedListOf(vararg elements: T): SortedList<T> =
    elements.asIterable().toSortedList()

fun <T : Comparable<T>> Sequence<T>.toSortedListFromPreSorted(): SortedList<T> =
    SortedList(delegate = toMutableList())

fun <T : Comparable<T>> Iterable<T>.toSortedListFromPreSorted(): SortedList<T> =
    SortedList(delegate = toMutableList())

fun <T : Comparable<T>> List<T>.toSortedListFromPreSorted(): SortedList<T> =
    SortedList(delegate = this as MutableList<T>)

fun <T : Comparable<T>> Array<T>.toSortedList(): SortedList<T> =
    asIterable().toSortedList()

fun <T : Comparable<T>> Sequence<T>.toSortedList(): SortedList<T> =
    asIterable().toSortedList()

fun <T : Comparable<T>> Iterable<T>.toSortedList(): SortedList<T> =
    SortedList(this)

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
    mapTo(TreeSet<R>()) {
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
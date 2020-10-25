package org.knowledger.collections

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <T> emptySortedList(comparator: Comparator<T>): SortedList<T> =
    DelegatedSortedList(comparator)

fun <T : Comparable<T>> emptySortedList(): SortedList<T> =
    emptySortedList { o1, o2 -> o1.compareTo(o2) }

fun <T> mutableSortedListOf(
    comparator: Comparator<T>, vararg elements: T,
): MutableSortedList<T> = elements.asIterable().toMutableSortedList(comparator)

fun <T : Comparable<T>> mutableSortedListOf(vararg elements: T): MutableSortedList<T> =
    elements.asIterable().toMutableSortedList()

fun <T> sortedListOf(comparator: Comparator<T>, vararg elements: T): SortedList<T> =
    elements.asIterable().toSortedList(comparator)

fun <T : Comparable<T>> sortedListOf(vararg elements: T): SortedList<T> =
    elements.asIterable().toSortedList()

fun <T : Comparable<T>> Sequence<T>.toMutableSortedListFromPreSorted(): MutableSortedList<T> =
    asIterable().toMutableSortedListFromPreSorted()

fun <T : Comparable<T>> Iterable<T>.toMutableSortedListFromPreSorted(): MutableSortedList<T> =
    toMutableSortedListFromPreSorted { o1, o2 -> o1.compareTo(o2) }

fun <T : Any> Iterable<T>.toMutableSortedListFromPreSorted(
    comparator: Comparator<T>,
): MutableSortedList<T> = DelegatedSortedList(comparator, toMutableList())

fun <T : Comparable<T>> Array<T>.toMutableSortedList(): MutableSortedList<T> =
    asIterable().toMutableSortedList()

fun <T : Comparable<T>> Sequence<T>.toMutableSortedList(): MutableSortedList<T> =
    asIterable().toMutableSortedList()

fun <T> Iterable<T>.toMutableSortedList(comparator: Comparator<T>): MutableSortedList<T> =
    DelegatedSortedList(comparator, this)

fun <T : Comparable<T>> Iterable<T>.toMutableSortedList(): MutableSortedList<T> =
    toMutableSortedList { o1, o2 -> o1.compareTo(o2) }


fun <T : Comparable<T>> Sequence<T>.toSortedListFromPreSorted(): SortedList<T> =
    asIterable().toSortedListFromPreSorted()

fun <T : Comparable<T>> Iterable<T>.toSortedListFromPreSorted(): SortedList<T> =
    toSortedListFromPreSorted { o1, o2 -> o1.compareTo(o2) }

fun <T> Iterable<T>.toSortedListFromPreSorted(comparator: Comparator<T>): SortedList<T> =
    DelegatedSortedList(comparator, toMutableList())


fun <T : Comparable<T>> Array<T>.toSortedList(): SortedList<T> =
    asIterable().toSortedList()

fun <T : Comparable<T>> Sequence<T>.toSortedList(): SortedList<T> =
    asIterable().toSortedList()

fun <T : Comparable<T>> Iterable<T>.toSortedList(): SortedList<T> =
    toSortedList { o1, o2 -> o1.compareTo(o2) }

fun <T> Iterable<T>.toSortedList(comparator: Comparator<T>): SortedList<T> =
    DelegatedSortedList(comparator, this)


fun <T> SortedList<T>.binarySearch(element: T): Int =
    binarySearch(element, comparator)

inline fun <T : Comparable<T>, R : Comparable<R>> SortedList<T>.searchAndGet(
    key: R, crossinline selector: (T) -> R,
): T? = getOrNull(binarySearchBy(key = key, selector = selector))

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

inline fun <T, R : Comparable<R>> Sequence<T>.mapSorted(map: (T) -> R): SortedList<R> =
    asIterable().mapSorted(map)


inline fun <T, R : Comparable<R>> Iterable<T>.mapSorted(map: (T) -> R): SortedList<R> =
    map(map).toSortedList()

inline fun <reified T> Sequence<T>.toSizedArray(i: Int): Array<T> {
    val iter = iterator()
    return Array(i) { iter.next() }
}

inline fun <reified T> Iterable<T>.toSizedArray(i: Int): Array<T> {
    val iter = iterator()
    return Array(i) { iter.next() }
}

inline fun <T, reified R> List<T>.mapToArray(transform: (T) -> R): Array<R> {
    contract {
        callsInPlace(transform, InvocationKind.UNKNOWN)
    }
    return Array(size) { transform(this[it]) }
}

inline fun <T, reified R> Array<out T>.mapToArray(transform: (T) -> R): Array<R> {
    contract {
        callsInPlace(transform, InvocationKind.UNKNOWN)
    }
    return Array(size) { transform(this[it]) }
}


inline fun <reified T> Array<out T>.fastPrefixAdd(prefix: T): Array<T> =
    if (isEmpty())
        arrayOf(prefix)
    else {
        val result = Array(size + 1) {
            this[(it + size - 1) % size]
        }
        result[0] = prefix
        result
    }

inline fun <T, reified R> Array<out T>.mapAndPrefixAdd(transform: (T) -> R, toAdd: T): Array<R> {
    contract {
        callsInPlace(transform, kind = InvocationKind.AT_LEAST_ONCE)
    }
    return if (isEmpty()) {
        arrayOf(transform(toAdd))
    } else {
        val result = Array(size + 1) { transform(this[(it + size - 1) % size]) }
        result[0] = transform(toAdd)
        result
    }
}

inline fun <T, reified R> Array<out T>.mapAndSuffixAdd(transform: (T) -> R, toAdd: T): Array<R> {
    contract {
        callsInPlace(transform, kind = InvocationKind.AT_LEAST_ONCE)
    }
    return if (isEmpty()) {
        arrayOf(transform(toAdd))
    } else {
        val result = Array(size + 1) { transform(this[it % size]) }
        result[size] = transform(toAdd)
        result
    }
}


inline fun <T> List<T>.filterByIndex(predicate: (Int) -> Boolean): List<T> {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }
    val mutList = ArrayList<T>(size / 2)
    forEachIndexed { i, elem ->
        if (predicate(i)) {
            mutList += elem
        }
    }
    return mutList
}

inline fun <T> Sequence<T>.filterByIndex(predicate: (Int) -> Boolean): List<T> =
    asIterable().filterByIndex(predicate)


inline fun <T> Iterable<T>.filterByIndex(predicate: (Int) -> Boolean): List<T> {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }
    return with(iterator()) {
        val mutList = mutableListOf<T>()
        var i = 0
        while (hasNext()) {
            if (predicate(i)) {
                mutList += next()
            }
            i++
        }
        mutList
    }
}


inline fun <E> MutableIterable<E>.removeByUnique(predicate: (E) -> Boolean): Boolean {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.next())) {
            it.remove()
            return true
        }
    }
    return false
}


inline fun <T, R> Collection<T>.mapMutableList(map: (T) -> R): MutableList<R> {
    contract {
        callsInPlace(map, InvocationKind.UNKNOWN)
    }
    val list = ArrayList<R>(size)
    forEach { list += map(it) }
    return list
}

inline fun <T, R> Collection<T>.mapMutableSet(map: (T) -> R): MutableSet<R> {
    contract {
        callsInPlace(map, InvocationKind.UNKNOWN)
    }
    val set = LinkedHashSet<R>(size)
    forEach { set += map(it) }
    return set
}

inline fun <T, R, S> Map<T, R>.mapMutable(map: (R) -> S): MutableMap<T, S> {
    contract {
        callsInPlace(map, InvocationKind.UNKNOWN)
    }
    val hashMap = LinkedHashMap<T, S>(size)

    forEach {
        hashMap[it.key] = map(it.value)
    }
    return hashMap
}


inline fun <T, R> Collection<T>.mapToSet(map: (T) -> R): Set<R> =
    mapMutableSet(map)


inline fun <T> Collection<T>.copy(clone: (T) -> T): List<T> =
    map(clone)

inline fun <T> Collection<T>.copyMutableList(clone: (T) -> T): MutableList<T> =
    mapMutableList(clone)

inline fun <T> Collection<T>.copySet(clone: (T) -> T): Set<T> =
    mapToSet(clone)

inline fun <T> Collection<T>.copyMutableSet(clone: (T) -> T): MutableSet<T> =
    mapMutableSet(clone)

inline fun <T : Comparable<T>> Collection<T>.copySortedList(clone: (T) -> T): SortedList<T> =
    map(clone).toSortedListFromPreSorted()

inline fun <T : Comparable<T>> Collection<T>.copyMutableSortedList(
    clone: (T) -> T,
): MutableSortedList<T> =
    map(clone).toMutableSortedListFromPreSorted()


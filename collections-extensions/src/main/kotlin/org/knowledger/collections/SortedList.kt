package org.knowledger.collections

interface SortedList<E> : List<E> {
    val comparator: Comparator<E>

    operator fun plus(list: SortedList<E>): SortedList<E>
    operator fun minus(list: SortedList<E>): SortedList<E>
}
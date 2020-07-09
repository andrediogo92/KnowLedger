package org.knowledger.collections

interface SortedList<E : Comparable<E>> : List<E> {
    operator fun plus(list: SortedList<E>): SortedList<E>
}
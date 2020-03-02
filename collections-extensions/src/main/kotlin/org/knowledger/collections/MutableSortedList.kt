package org.knowledger.collections

interface MutableSortedList<E : Comparable<E>> : SortedList<E>, MutableList<E> {
    operator fun plusAssign(element: E)
    operator fun plus(element: E): MutableSortedList<E>
    operator fun minus(element: E): MutableSortedList<E>
    operator fun minusAssign(element: E)
}
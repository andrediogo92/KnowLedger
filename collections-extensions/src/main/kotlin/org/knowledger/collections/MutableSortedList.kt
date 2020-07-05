package org.knowledger.collections

interface MutableSortedList<E : Comparable<E>> : SortedList<E>, MutableList<E> {
    operator fun plusAssign(element: E)
    operator fun minus(element: E): MutableSortedList<E>
    operator fun minusAssign(element: E)
    fun addWithIndex(element: E): Int
    fun removeWithIndex(element: E): Int
}
package org.knowledger.collections

interface MutableSortedList<E> : SortedList<E>, MutableList<E> {
    fun addWithIndex(element: E): Int
    fun removeWithIndex(element: E): Int
    fun replace(element: E): Boolean
    fun replaceAt(index: Int, element: E): Boolean
}
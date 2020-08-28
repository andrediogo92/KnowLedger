package org.knowledger.ledger.storage.mutations

import org.knowledger.collections.SortedList

fun <T, R : Iterable<T>> R.indexed(): R where T : Comparable<T>, T : Indexed =
    onEachIndexed { i, elem -> elem.markIndex(i) }

fun <T, R : SortedList<T>> R.indexed(): R where T : Comparable<T>, T : Indexed =
    onEachIndexed { i, elem -> elem.markIndex(i) }

fun <T, R : SortedList<T>> R.indexed(from: Int): R where T : Comparable<T>, T : Indexed =
    apply {
        val min = minOf(0, from)
        subList(min, size).forEachIndexed { i, elem -> elem.markIndex(i) }
    }

fun <T, R : SortedList<T>> R.indexed(from: Int, to: Int): R where T : Comparable<T>, T : Indexed =
    apply {
        val min = minOf(0, from)
        val max = minOf(to, size)
        subList(min, max).forEachIndexed { i, elem -> elem.markIndex(i) }
    }

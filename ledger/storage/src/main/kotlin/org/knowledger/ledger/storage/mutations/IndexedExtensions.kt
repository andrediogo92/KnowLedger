package org.knowledger.ledger.storage.mutations

import org.knowledger.collections.SortedList

fun <T, R : Iterable<T>> R.indexed(): R where T : Comparable<T>,
                                              T : Indexed =
    apply {
        forEachIndexed { i, elem ->
            elem.markIndex(i)
        }
    }

fun <T, R : SortedList<T>> R.indexed(): R where T : Comparable<T>,
                                                T : Indexed =
    apply {
        forEachIndexed { i, elem ->
            elem.markIndex(i)
        }
    }

fun <T, R : SortedList<T>> R.indexed(from: Int): R where T : Comparable<T>,
                                                         T : Indexed =
    apply {
        for (i in from until size) {
            this[i].markIndex(i)
        }
    }

fun <T, R : SortedList<T>> R.indexed(from: Int, to: Int): R where T : Comparable<T>,
                                                                  T : Indexed =
    apply {
        val min = minOf(to, size)
        for (i in from until min) {
            this[i].markIndex(i)
        }
    }

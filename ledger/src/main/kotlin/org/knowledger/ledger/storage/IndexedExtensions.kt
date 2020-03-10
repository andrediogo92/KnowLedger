package org.knowledger.ledger.storage

internal fun <T, R : Iterable<T>> R.indexed(): R
        where T : Comparable<T> =
    apply {
        forEachIndexed { i, elem ->
            (elem as Indexed).markIndex(i)
        }
    }

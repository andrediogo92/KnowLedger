package org.knowledger.ledger.storage

internal interface Indexed {
    val index: Int
    fun markIndex(index: Int)
}
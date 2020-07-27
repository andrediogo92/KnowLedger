package org.knowledger.ledger.storage.mutations

interface Indexed {
    val index: Int
    fun markIndex(index: Int)
}
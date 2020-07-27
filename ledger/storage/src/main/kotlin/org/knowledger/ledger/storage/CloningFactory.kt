package org.knowledger.ledger.storage

interface CloningFactory<T> {
    fun create(other: T): T
}
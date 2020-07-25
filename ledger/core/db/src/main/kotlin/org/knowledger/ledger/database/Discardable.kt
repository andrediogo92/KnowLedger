package org.knowledger.ledger.database

interface Discardable<out T : Any> {
    fun discard(): T
}
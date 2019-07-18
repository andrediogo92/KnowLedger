package org.knowledger.ledger.core.database

interface Discardable<out T : Any> {
    fun discard(): T
}
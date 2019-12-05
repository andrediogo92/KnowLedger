package org.knowledger.ledger.adapters

import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement

/**
 * EagerStorable immediately persists every nested [EagerStorable]
 * via [persist] before returning.
 */
interface EagerStorable<in T> {
    fun persist(
        toStore: T,
        session: ManagedSession
    ): StorageElement =
        store(toStore, session).apply {
            session.save(this)
        }

    fun store(toStore: T, session: ManagedSession): StorageElement
}
package org.knowledger.ledger.database.adapters

import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement

/**
 * Describes a contract for storing any type of value into
 * a persistent storage by acessing a [NewInstanceSession]
 * capable of creating [StorageElement]s and the value
 * being stored.
 *
 * The result must be a storage element that describes all
 * the contained properties in [T].
 *
 * [T] may have properties which themselves have [Storable]
 * implementations in order to produce storage elements
 * to link.
 */
interface Storable<in T> {
    fun store(
        toStore: T,
        session: NewInstanceSession
    ): StorageElement
}
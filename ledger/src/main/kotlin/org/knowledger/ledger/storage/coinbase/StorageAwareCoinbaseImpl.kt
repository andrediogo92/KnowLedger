package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.adapters.AdapterCollection
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StoragePairs

internal class StorageAwareCoinbaseImpl(
    adapterCollection: AdapterCollection,
    override val coinbase: MutableCoinbase
) : StorageAwareCoinbase, MutableCoinbase by coinbase {
    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.Linked(
                "witnesses",
                adapterCollection.witnessStorageAdapter
            ), StoragePairs.Linked(
                "header",
                adapterCollection.coinbaseHeaderStorageAdapter
            ), StoragePairs.Linked(
                "merkleTree",
                adapterCollection.merkleTreeStorageAdapter
            )
        )

    override var id: StorageID? = null

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)

    override fun equals(other: Any?): Boolean =
        coinbase == other

    override fun hashCode(): Int =
        coinbase.hashCode()
}
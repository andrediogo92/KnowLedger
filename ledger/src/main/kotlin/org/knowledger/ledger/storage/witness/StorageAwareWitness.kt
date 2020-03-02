package org.knowledger.ledger.storage.witness

import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.updateLinked


internal data class StorageAwareWitness(
    internal val transactionOutput: HashedWitnessImpl
) : HashedWitness by transactionOutput,
    StorageAware<HashedWitness> {
    internal constructor(
        adapterManager: AdapterManager,
        witness: HashedWitnessImpl
    ) : this(witness) {
        _invalidated = arrayOf(
            StoragePairs.Hash("hash"),
            StoragePairs.LinkedList(
                "transactionOutputs",
                adapterManager.transactionOutputStorageAdapter
            )
        )
    }

    override var id: StorageID? = null

    private var _invalidated: Array<StoragePairs<*>> =
        emptyArray()

    override val invalidated: Array<StoragePairs<*>>
        get() = _invalidated

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)

    override fun equals(other: Any?): Boolean =
        transactionOutput == other

    override fun hashCode(): Int =
        transactionOutput.hashCode()
}
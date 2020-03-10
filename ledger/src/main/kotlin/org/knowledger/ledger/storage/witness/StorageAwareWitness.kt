package org.knowledger.ledger.storage.witness

import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.Indexed
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.updateLinked


internal class StorageAwareWitness private constructor(
    internal val witness: HashedWitnessImpl
) : HashedWitness by witness, Indexed by witness,
    PayoutAdding by witness, StorageAware<HashedWitness> {
    override var id: StorageID? = null

    private var _invalidated: Array<StoragePairs<*>> =
        emptyArray()

    override val invalidated: Array<StoragePairs<*>>
        get() = _invalidated

    internal constructor(
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter,
        witness: HashedWitnessImpl
    ) : this(witness) {
        _invalidated = arrayOf(
            StoragePairs.Hash("hash"),
            StoragePairs.Native("index"),
            StoragePairs.LinkedList(
                "transactionOutputs",
                transactionOutputStorageAdapter
            )
        )
    }

    override fun addToPayout(transactionOutput: TransactionOutput) {
        witness.addToPayout(transactionOutput)
        if (id != null) {
            invalidated.replace(0, hash)
            invalidated.replace(2, transactionOutputs)
        }
    }

    override fun markIndex(index: Int) {
        if (index != this.index) {
            witness.markIndex(index)
            if (id != null) {
                invalidated.replace(1, index)
            }
        }
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)


    override fun equals(other: Any?): Boolean =
        witness == other

    override fun hashCode(): Int =
        witness.hashCode()
}
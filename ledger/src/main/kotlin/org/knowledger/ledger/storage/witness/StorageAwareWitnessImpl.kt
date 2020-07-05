package org.knowledger.ledger.storage.witness

import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.replace


internal class StorageAwareWitnessImpl(
    override val witness: MutableHashedWitness,
    override val invalidated: Array<StoragePairs<*>>
) : MutableHashedWitness by witness, StorageAwareWitness {
    override var id: StorageID? = null

    override fun addToPayout(transactionOutput: TransactionOutput) {
        witness.addToPayout(transactionOutput)
        if (id != null) {
            invalidated.replace(2, transactionOutputs)
        }
    }

    override fun markIndex(index: Int) {
        witness.markIndex(index)
        if (id != null) {
            invalidated.replace(1, index)
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
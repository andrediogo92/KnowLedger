package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.simpleUpdate


internal data class StorageAwareTransactionOutput(
    internal val transactionOutput: HashedTransactionOutputImpl
) : HashedTransactionOutput by transactionOutput, StorageAware<HashedTransactionOutput> {
    override var id: StorageID? = null

    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.Hash("hash"),
            StoragePairs.HashSet("txSet")
        )

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidated)

    override fun equals(other: Any?): Boolean =
        transactionOutput == other

    override fun hashCode(): Int =
        transactionOutput.hashCode()
}
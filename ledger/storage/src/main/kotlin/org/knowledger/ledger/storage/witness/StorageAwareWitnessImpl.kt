package org.knowledger.ledger.storage.witness

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.cache.StoragePairs
import org.knowledger.ledger.storage.cache.replaceUnchecked


internal class StorageAwareWitnessImpl(
    override val witness: MutableHashedWitness
) : MutableHashedWitness by witness, StorageAwareWitness {
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = arrayOf(
        StoragePairs.LinkedHash("hash"),
        StoragePairs.Native("index"),
        StoragePairs.LinkedList<TransactionOutput>(
            "transactionOutputs", AdapterIds.TransactionOutput
        )
    )

    @Suppress("UNCHECKED_CAST")
    override fun addToPayout(transactionOutput: TransactionOutput) {
        witness.addToPayout(transactionOutput)
        if (id != null) {
            invalidated.replaceUnchecked(2, mutableTransactionOutputs)
        }
    }

    override fun markIndex(index: Int) {
        witness.markIndex(index)
        if (id != null) {
            invalidated.replaceUnchecked(1, index)
        }
    }

    override fun equals(other: Any?): Boolean =
        witness == other

    override fun hashCode(): Int =
        witness.hashCode()
}
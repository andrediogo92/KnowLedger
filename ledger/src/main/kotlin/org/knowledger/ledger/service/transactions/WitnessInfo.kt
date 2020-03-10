package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.adapters.StorageLoadable

data class WitnessInfo(
    val hash: Hash, val index: Int,
    val max: Long
) : LedgerContract {
    companion object : StorageLoadable<WitnessInfo> {
        override fun load(
            ledgerHash: Hash, element: StorageElement
        ): Outcome<WitnessInfo, LoadFailure> =
            tryOrLoadUnknownFailure {
                val hash = element.getHashProperty("hash")
                val index: Int = element.getStorageProperty("index")
                val max: Long = element.getStorageProperty("max")

                Outcome.Ok(WitnessInfo(hash, index, max))
            }
    }
}
package org.knowledger.ledger.adapters.service.loadables

import org.knowledger.ledger.adapters.service.ServiceLoadable
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.data.WitnessInfo
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.results.LoadFailure

internal class WitnessInfoServiceLoadable : ServiceLoadable<WitnessInfo> {
    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<WitnessInfo, LoadFailure> =
        with(element) {
            val hash = getHashProperty("hash")
            val index: Int = getStorageProperty("index")
            val max: Long = getStorageProperty("max")

            WitnessInfo(hash, index, max).ok()
        }
}
package org.knowledger.ledger.adapters.config

import com.github.michaelbull.result.binding
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewHash
import org.knowledger.ledger.service.solver.pushNewLinked
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class ChainIdStorageAdapter : LedgerStorageAdapter<ChainId> {
    override val id: String
        get() = "ChainId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "hash" to StorageType.HASH,
            "ledgerHash" to StorageType.HASH,
            "tag" to StorageType.HASH,
            "blockParams" to StorageType.LINK,
            "coinbaseParams" to StorageType.LINK
        )

    override fun store(
        element: ChainId, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewHash("hash", element.hash)
                pushNewHash("ledgerHash", element.ledgerHash)
                pushNewHash("tag", element.tag)
                pushNewLinked("blockParams", element.blockParams, AdapterIds.BlockParams)
                pushNewLinked("coinbaseParams", element.coinbaseParams, AdapterIds.CoinbaseParams)
            }.ok()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<ChainId, LoadFailure> =
        element.cachedLoad {
            tryOrLoadUnknownFailure {
                val blockParamsElem = element.getLinked("blockParams")
                val coinbaseParamsElem = element.getLinked("coinbaseParams")
                binding<ChainId, LoadFailure> {
                    val blockParams = context.blockParamsStorageAdapter.load(
                        ledgerHash, blockParamsElem, context
                    ).bind()
                    val coinbaseParams = context.coinbaseParamsStorageAdapter.load(
                        ledgerHash, coinbaseParamsElem, context
                    ).bind()
                    val hash: Hash = element.getHashProperty("hash")
                    val ledger: Hash = element.getHashProperty("ledgerHash")
                    val tag: Hash = element.getHashProperty("tag")
                    assert(ledger == ledgerHash)
                    context.chainIdFactory.create(hash, ledger, tag, blockParams, coinbaseParams)
                }
            }
        }
}

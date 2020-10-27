package org.knowledger.ledger.adapters.config

import com.github.michaelbull.result.binding
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.core.adapters.Tag
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.results.LoadFailure

internal class ChainIdStorageAdapter : LedgerStorageAdapter<ChainId> {
    override val id: String get() = "ChainId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "hash" to StorageType.HASH,
            "ledgerHash" to StorageType.HASH,
            "tag" to StorageType.STRING,
            "rawTag" to StorageType.HASH,
            "blockParams" to StorageType.LINK,
            "coinbaseParams" to StorageType.LINK,
        )

    override fun store(element: ChainId, state: StorageState): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewHash("hash", element.hash)
            pushNewHash("ledgerHash", element.ledgerHash)
            pushNewNative("tag", element.tag.id)
            pushNewHash("rawTag", element.rawTag)
            pushNewLinked("blockParams", element.blockParams, AdapterIds.BlockParams)
            pushNewLinked("coinbaseParams", element.coinbaseParams, AdapterIds.CoinbaseParams)
        }.ok()


    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<ChainId, LoadFailure> =
        element.cachedLoad {
            val blockParamsElem = getLinked("blockParams")
            val coinbaseParamsElem = getLinked("coinbaseParams")
            binding {
                val blockParams = context.blockParamsStorageAdapter.load(
                    ledgerHash, blockParamsElem, context
                ).bind()
                val coinbaseParams = context.coinbaseParamsStorageAdapter.load(
                    ledgerHash, coinbaseParamsElem, context
                ).bind()
                val hash: Hash = getHashProperty("hash")
                val ledger: Hash = getHashProperty("ledgerHash")
                val tag = Tag(getStorageProperty("tag"))
                val rawTag: Hash = getHashProperty("rawTag")
                assert(ledger == ledgerHash)
                context.chainIdFactory.create(hash, ledger, tag, rawTag, blockParams,
                                              coinbaseParams)
            }
        }
}

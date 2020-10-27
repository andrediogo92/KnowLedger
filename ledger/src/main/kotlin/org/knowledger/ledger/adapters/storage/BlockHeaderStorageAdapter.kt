package org.knowledger.ledger.adapters.storage

import com.github.michaelbull.result.binding
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.results.LoadFailure

internal class BlockHeaderStorageAdapter : LedgerStorageAdapter<MutableBlockHeader> {
    override val id: String get() = "BlockHeader"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainHash" to StorageType.HASH,
            "hash" to StorageType.HASH,
            "merkleRoot" to StorageType.HASH,
            "previousHash" to StorageType.HASH,
            "blockParams" to StorageType.LINK,
            "seconds" to StorageType.LONG,
            "nonce" to StorageType.LONG,
        )


    override fun store(
        element: MutableBlockHeader, state: StorageState,
    ): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewHash("chainHash", element.chainHash)
            pushNewHash("hash", element.hash)
            pushNewHash("merkleRoot", element.merkleRoot)
            pushNewHash("previousHash", element.previousHash)
            pushNewLinked("blockParams", element.blockParams, AdapterIds.BlockParams)
            pushNewNative("seconds", element.seconds)
            pushNewNative("nonce", element.nonce)
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<MutableBlockHeader, LoadFailure> =
        element.cachedLoad {
            val blockParamsElem = getLinked("blockParams")
            binding {
                val blockParams = context.blockParamsStorageAdapter.load(
                    ledgerHash, blockParamsElem, context
                ).bind()
                val chainHash = getHashProperty("chainHash")
                val hash = getHashProperty("hash")
                val merkleRoot = getHashProperty("merkleRoot")
                val previousHash = getHashProperty("previousHash")
                val seconds: Long = getStorageProperty("seconds")
                val nonce: Long = getStorageProperty("nonce")

                context.blockHeaderFactory.create(
                    chainHash, hash, previousHash, blockParams, merkleRoot, seconds, nonce,
                )
            }

        }

}
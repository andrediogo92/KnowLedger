package org.knowledger.ledger.storage.block.header

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.adapters.BlockParamsStorageAdapter
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.intoLoad
import org.knowledger.ledger.results.mapFailure
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.results.zip
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.block.header.factory.HashedBlockHeaderFactory

internal class SUBlockHeaderStorageAdapter(
    private val blockHeaderFactory: HashedBlockHeaderFactory,
    private val chainIdStorageAdapter: ServiceStorageAdapter<ChainId>
) : LedgerStorageAdapter<MutableHashedBlockHeader> {
    private val blockParamsStorageAdapter = BlockParamsStorageAdapter

    override val id: String
        get() = "BlockHeader"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "hash" to StorageType.HASH,
            "merkleRoot" to StorageType.HASH,
            "previousHash" to StorageType.HASH,
            "blockParams" to StorageType.LINK,
            "seconds" to StorageType.LONG,
            "nonce" to StorageType.LONG
        )

    override fun store(
        toStore: MutableHashedBlockHeader, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked(
                "chainId",
                chainIdStorageAdapter.persist(
                    toStore.chainId, session
                )
            ).setHashProperty("hash", toStore.hash)
            .setHashProperty("merkleRoot", toStore.merkleRoot)
            .setHashProperty("previousHash", toStore.previousHash)
            .setLinked(
                "blockParams",
                blockParamsStorageAdapter.persist(
                    toStore.params, session
                )
            ).setStorageProperty(
                "seconds", toStore.seconds
            ).setStorageProperty("nonce", toStore.nonce)

    @Suppress("NAME_SHADOWING")
    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<MutableHashedBlockHeader, LoadFailure> =
        tryOrLoadUnknownFailure {
            val chainId = element.getLinked("chainId")
            val blockParams = element.getLinked("blockParams")
            zip(
                chainIdStorageAdapter.load(
                    ledgerHash, chainId
                ).mapFailure { it.intoLoad() },
                blockParamsStorageAdapter.load(
                    ledgerHash, blockParams
                ).mapFailure { it.intoLoad() }
            ) { chainId, blockParams ->
                val hash =
                    element.getHashProperty("hash")

                val merkleRoot =
                    element.getHashProperty("merkleRoot")

                val previousHash =
                    element.getHashProperty("previousHash")

                val seconds: Long =
                    element.getStorageProperty("seconds")

                val nonce: Long =
                    element.getStorageProperty("nonce")

                blockHeaderFactory.create(
                    chainId, blockParams, previousHash, hash,
                    merkleRoot, seconds, nonce
                )
            }

        }
}
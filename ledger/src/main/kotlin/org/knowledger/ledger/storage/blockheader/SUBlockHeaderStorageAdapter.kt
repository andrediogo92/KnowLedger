package org.knowledger.ledger.storage.blockheader

import org.knowledger.ledger.config.adapters.BlockParamsStorageAdapter
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.flatZip
import org.knowledger.ledger.results.intoLoad
import org.knowledger.ledger.results.mapFailure
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal class SUBlockHeaderStorageAdapter(
    private val container: LedgerInfo
) : LedgerStorageAdapter<HashedBlockHeaderImpl> {
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
        toStore: HashedBlockHeaderImpl, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked(
                "chainId",
                ChainIdStorageAdapter.persist(
                    toStore.chainId, session
                )
            ).setHashProperty("hash", toStore.hash)
            .setHashProperty("merkleRoot", toStore.merkleRoot)
            .setHashProperty("previousHash", toStore.previousHash)
            .setLinked(
                "blockParams",
                BlockParamsStorageAdapter.persist(
                    toStore.params, session
                )
            ).setStorageProperty(
                "seconds", toStore.seconds
            ).setStorageProperty("nonce", toStore.nonce)

    @Suppress("NAME_SHADOWING")
    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<HashedBlockHeaderImpl, LoadFailure> =
        tryOrLoadUnknownFailure {
            val chainId = element.getLinked("chainId")
            val blockParams = element.getLinked("blockParams")
            flatZip(
                ChainIdStorageAdapter.load(
                    ledgerHash, chainId
                ).mapFailure { it.intoLoad() },
                BlockParamsStorageAdapter.load(
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

                Outcome.Ok(
                    HashedBlockHeaderImpl(
                        chainId, container
                            .hasher, container
                            .encoder,
                        hash, blockParams, previousHash,
                        merkleRoot, seconds, nonce
                    )
                )
            }

        }
}
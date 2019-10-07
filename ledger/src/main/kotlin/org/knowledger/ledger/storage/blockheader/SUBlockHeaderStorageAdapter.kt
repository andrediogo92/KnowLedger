package org.knowledger.ledger.storage.blockheader

import org.knowledger.ledger.config.adapters.loadBlockParams
import org.knowledger.ledger.config.adapters.loadChainId
import org.knowledger.ledger.config.adapters.persist
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.flatZip
import org.knowledger.ledger.core.results.mapFailure
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.results.intoLoad
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal object SUBlockHeaderStorageAdapter : LedgerStorageAdapter<HashedBlockHeaderImpl> {
    override val id: String
        get() = BlockHeaderStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = BlockHeaderStorageAdapter.properties

    override fun store(
        toStore: HashedBlockHeaderImpl, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(BlockHeaderStorageAdapter.id)
            .setLinked(
                "chainId",
                toStore.chainId.persist(session)
            ).setHashProperty("hash", toStore.hash)
            .setHashProperty("merkleRoot", toStore.merkleRoot)
            .setHashProperty("previousHash", toStore.previousHash)
            .setLinked(
                "blockParams",
                toStore.params.persist(session)
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
                chainId
                    .loadChainId(ledgerHash)
                    .mapFailure { it.intoLoad() },
                blockParams
                    .loadBlockParams(ledgerHash)
                    .mapFailure { it.intoLoad() }
            ) { chainId, blockParams ->
                val container: LedgerContainer? =
                    LedgerHandle.getContainer(chainId.ledgerHash)
                container?.let {
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
                            chainId, it.hasher, it.encoder,
                            hash, blockParams, previousHash,
                            merkleRoot, seconds, nonce
                        )
                    )
                } ?: Outcome.Error(
                    LoadFailure.NoMatchingContainer(
                        ledgerHash
                    )
                )
            }

        }
}
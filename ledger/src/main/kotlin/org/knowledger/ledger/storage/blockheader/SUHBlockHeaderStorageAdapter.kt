package org.knowledger.ledger.storage.blockheader

import org.knowledger.ledger.config.adapters.BlockParamsStorageAdapter
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.mapFailure
import org.knowledger.ledger.core.results.zip
import org.knowledger.ledger.results.intoLoad
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal object SUHBlockHeaderStorageAdapter : LedgerStorageAdapter<HashedBlockHeaderImpl> {
    override val id: String
        get() = BlockHeaderStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = BlockHeaderStorageAdapter.properties

    override fun store(
        toStore: HashedBlockHeaderImpl, session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(BlockHeaderStorageAdapter.id)
            .setLinked(
                "chainId", ChainIdStorageAdapter,
                toStore.chainId, session
            ).setHashProperty("hash", toStore.hash)
            .setHashProperty("merkleRoot", toStore.merkleRoot)
            .setHashProperty("previousHash", toStore.previousHash)
            .setLinked(
                "ledgerParams", BlockParamsStorageAdapter,
                toStore.params, session
            ).setStorageProperty(
                "seconds", toStore.seconds
            ).setStorageProperty("nonce", toStore.nonce)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<HashedBlockHeaderImpl, LoadFailure> =
        tryOrLoadUnknownFailure {
            val hash =
                element.getHashProperty("hash")

            val merkleRoot =
                element.getHashProperty("merkleRoot")

            val previousHash =
                element.getHashProperty("previousHash")

            zip(
                ChainIdStorageAdapter.load(
                    ledgerHash,
                    element.getLinked("chainId")
                ),
                BlockParamsStorageAdapter.load(
                    ledgerHash,
                    element.getLinked("ledgerParams")
                )
            ) { chainId, blockParams ->
                val seconds: Long =
                    element.getStorageProperty("seconds")

                val nonce: Long =
                    element.getStorageProperty("nonce")

                LedgerHandle.getContainer(chainId.ledgerHash)!!.let {
                    HashedBlockHeaderImpl(
                        chainId,
                        it.hasher,
                        it.cbor,
                        hash,
                        blockParams,
                        previousHash,
                        merkleRoot,
                        seconds,
                        nonce
                    )
                }
            }.mapFailure {
                it.intoLoad()
            }

        }
}
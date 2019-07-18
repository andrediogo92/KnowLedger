package org.knowledger.ledger.storage.blockheader

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.mapFailure
import org.knowledger.common.results.zip
import org.knowledger.ledger.config.adapters.BlockParamsStorageAdapter
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.results.intoLoad
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import java.time.Instant

object SUBlockHeaderStorageAdapter : LedgerStorageAdapter<StorageUnawareBlockHeader> {
    override val id: String
        get() = BlockHeaderStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = BlockHeaderStorageAdapter.properties

    override fun store(
        toStore: StorageUnawareBlockHeader, session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(BlockHeaderStorageAdapter.id)
            .setLinked(
                "chainId", ChainIdStorageAdapter,
                toStore.chainId, session
            ).setDifficultyProperty(
                "difficulty", toStore.difficulty, session
            ).setStorageProperty("blockheight", toStore.blockheight)
            .setHashProperty("hashId", toStore.hashId)
            .setHashProperty("merkleRoot", toStore.merkleRoot)
            .setHashProperty("previousHash", toStore.previousHash)
            .setLinked(
                "ledgerParams", BlockParamsStorageAdapter,
                toStore.params, session
            ).setStorageProperty(
                "seconds", toStore.timestamp.epochSecond
            ).setStorageProperty("nanos", toStore.timestamp.nano)
            .setStorageProperty("nonce", toStore.nonce)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageUnawareBlockHeader, LoadFailure> =
        tryOrLoadUnknownFailure {
            val difficulty =
                element.getDifficultyProperty("difficulty")

            val blockheight: Long =
                element.getStorageProperty("blockheight")

            val hash =
                element.getHashProperty("hashId")

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

                val nanos: Int =
                    element.getStorageProperty("nanos")

                val nonce: Long =
                    element.getStorageProperty("nonce")

                val instant = Instant.ofEpochSecond(
                    seconds,
                    nanos.toLong()
                )
                StorageUnawareBlockHeader(
                    chainId,
                    LedgerHandle.getHasher(chainId.ledgerHash)!!,
                    difficulty,
                    blockheight,
                    hash,
                    blockParams,
                    previousHash,
                    merkleRoot,
                    instant,
                    nonce
                )
            }.mapFailure {
                it.intoLoad()
            }


        }
}
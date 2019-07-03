package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.mapFailure
import pt.um.masb.common.results.zip
import pt.um.masb.ledger.config.adapters.BlockParamsStorageAdapter
import pt.um.masb.ledger.config.adapters.ChainIdStorageAdapter
import pt.um.masb.ledger.results.intoLoad
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.handles.LedgerHandle
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.BlockHeader
import java.time.Instant

object BlockHeaderStorageAdapter : LedgerStorageAdapter<BlockHeader> {
    override val id: String
        get() = "BlockHeader"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "difficulty" to StorageType.DIFFICULTY,
            "blockheight" to StorageType.LONG,
            "hashId" to StorageType.HASH,
            "merkleRoot" to StorageType.HASH,
            "previousHash" to StorageType.HASH,
            "ledgerParams" to StorageType.LINK,
            "seconds" to StorageType.LONG,
            "nanos" to StorageType.INTEGER,
            "nonce" to StorageType.LONG
        )

    override fun store(
        toStore: BlockHeader,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setLinked(
                "chainId", ChainIdStorageAdapter,
                toStore.chainId, session
            )
            setDifficultyProperty(
                "difficulty", toStore.difficulty, session
            )
            setStorageProperty("blockheight", toStore.blockheight)
            setHashProperty("hashId", toStore.hashId)
            setHashProperty("merkleRoot", toStore.merkleRoot)
            setHashProperty("previousHash", toStore.previousHash)
            setLinked(
                "ledgerParams", BlockParamsStorageAdapter,
                toStore.params, session
            )
            setStorageProperty(
                "seconds", toStore.timestamp.epochSecond
            )
            setStorageProperty("nanos", toStore.timestamp.nano)
            setStorageProperty("nonce", toStore.nonce)
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<BlockHeader, LoadFailure> =
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
                BlockHeader(
                    chainId,
                    LedgerHandle.getHasher(chainId.ledgerHash)!!,
                    difficulty,
                    blockheight,
                    hash,
                    merkleRoot,
                    previousHash,
                    blockParams,
                    instant,
                    nonce
                )
            }.mapFailure {
                it.intoLoad()
            }


        }
}
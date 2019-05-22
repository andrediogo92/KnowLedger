package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.ledger.config.adapters.BlockParamsStorageAdapter
import pt.um.masb.ledger.results.intoLoad
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.BlockHeader
import java.time.Instant

class BlockHeaderStorageAdapter : LedgerStorageAdapter<BlockHeader> {
    val blockParamsStorageAdapter = BlockParamsStorageAdapter()

    override val id: String
        get() = "BlockHeader"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "ledgerHash" to StorageType.BYTES,
            "difficulty" to StorageType.BYTES,
            "blockheight" to StorageType.LONG,
            "hashId" to StorageType.BYTES,
            "merkleRoot" to StorageType.BYTES,
            "previousHash" to StorageType.BYTES,
            "params" to StorageType.LINK,
            "seconds" to StorageType.LONG,
            "nanos" to StorageType.INTEGER,
            "nonce" to StorageType.LONG
        )

    override fun store(
        toStore: BlockHeader,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setHashProperty("ledgerHash", toStore.ledgerId)
            setDifficultyProperty(
                "difficulty", toStore.difficulty, session
            )
            setStorageProperty("blockheight", toStore.blockheight)
            setHashProperty("hashId", toStore.hashId)
            setHashProperty("merkleRoot", toStore.merkleRoot)
            setHashProperty("previousHash", toStore.previousHash)
            setLinked(
                "params", blockParamsStorageAdapter,
                toStore.params, session
            )
            setStorageProperty(
                "seconds", toStore.timestamp.epochSecond
            )
            setStorageProperty("nanos", toStore.timestamp.nano)
            setStorageProperty("nonce", toStore.nonce)
        }

    override fun load(
        hash: Hash,
        element: StorageElement
    ): LoadResult<BlockHeader> =
        tryOrLoadQueryFailure {
            val blid =
                element.getHashProperty("ledgerHash")

            assert(blid.contentEquals(hash))

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

            val params = blockParamsStorageAdapter.load(
                hash,
                element.getLinked("params")
            )
            if (params !is LoadResult.Success) {
                return@tryOrLoadQueryFailure params.intoLoad<BlockHeader>()
            }

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

            LoadResult.Success(
                BlockHeader(
                    blid,
                    difficulty,
                    blockheight,
                    hash,
                    merkleRoot,
                    previousHash,
                    params.data,
                    instant,
                    nonce
                )
            )

        }
}
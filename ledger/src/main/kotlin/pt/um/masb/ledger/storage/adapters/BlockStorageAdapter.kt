package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.ledger.data.adapters.MerkleTreeStorageAdapter
import pt.um.masb.ledger.results.collapse
import pt.um.masb.ledger.results.intoLoad
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.results.LoadListResult
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.Block

class BlockStorageAdapter : LedgerStorageAdapter<Block> {
    val coinbaseStorageAdapter = CoinbaseStorageAdapter()
    val transactionStorageAdapter = TransactionStorageAdapter()
    val blockHeaderStorageAdapter = BlockHeaderStorageAdapter()
    val merkleTreeStorageAdapter = MerkleTreeStorageAdapter()

    override val id: String
        get() = "Block"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "data" to StorageType.LIST,
            "coinbase" to StorageType.LINK,
            "header" to StorageType.LINK,
            "merkleTree" to StorageType.LINK
        )

    override fun store(
        toStore: Block,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setElementList(
                "data",
                toStore.data.map {
                    transactionStorageAdapter.store(
                        it, session
                    )
                }
            )
            setLinked(
                "coinbase", coinbaseStorageAdapter,
                toStore.coinbase, session
            )
            setLinked(
                "header", blockHeaderStorageAdapter,
                toStore.header, session
            )
            setLinked(
                "merkleTree", merkleTreeStorageAdapter,
                toStore.merkleTree, session
            )
        }


    override fun load(
        hash: Hash, element: StorageElement
    ): LoadResult<Block> =
        tryOrLoadQueryFailure {
            val data: List<StorageElement> =
                element.getElementList("data")

            val listT =
                data.asSequence()
                    .map {
                        transactionStorageAdapter.load(
                            hash,
                            it
                        )
                    }.collapse()

            if (listT !is LoadListResult.Success) {
                return@tryOrLoadQueryFailure listT.intoLoad<Block>()
            }

            val coinbase =
                coinbaseStorageAdapter.load(
                    hash,
                    element.getLinked(
                        "coinbase"
                    )
                )

            if (coinbase !is LoadResult.Success) {
                return@tryOrLoadQueryFailure coinbase.intoLoad<Block>()
            }

            val header =
                blockHeaderStorageAdapter.load(
                    hash,
                    element.getLinked(
                        "header"
                    )
                )

            if (header !is LoadResult.Success) {
                return@tryOrLoadQueryFailure header.intoLoad<Block>()
            }

            val merkleTree =
                merkleTreeStorageAdapter.load(
                    hash,
                    element.getLinked(
                        "merkleTree"
                    )
                )

            if (merkleTree !is LoadResult.Success) {
                return@tryOrLoadQueryFailure merkleTree.intoLoad<Block>()
            }

            LoadResult.Success(
                Block(
                    listT.data.toMutableList(),
                    coinbase.data,
                    header.data,
                    merkleTree.data
                )
            )
        }
}
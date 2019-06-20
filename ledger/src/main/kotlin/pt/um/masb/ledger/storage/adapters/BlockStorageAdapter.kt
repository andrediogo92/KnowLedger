package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.allValues
import pt.um.masb.common.results.flatMapSuccess
import pt.um.masb.common.results.mapSuccess
import pt.um.masb.ledger.data.adapters.MerkleTreeStorageAdapter
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.storage.BlockHeader
import pt.um.masb.ledger.storage.Coinbase
import pt.um.masb.ledger.storage.Transaction

object BlockStorageAdapter : LedgerStorageAdapter<Block> {
    override val id: String
        get() = "Block"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "value" to StorageType.LIST,
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
                "value",
                toStore.data.map {
                    TransactionStorageAdapter.store(
                        it, session
                    )
                }
            ).setLinked(
                "coinbase", CoinbaseStorageAdapter,
                toStore.coinbase, session
            ).setLinked(
                "header", BlockHeaderStorageAdapter,
                toStore.header, session
            ).setLinked(
                "merkleTree", MerkleTreeStorageAdapter,
                toStore.merkleTree, session
            )
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<Block, LoadFailure> =
        tryOrLoadUnknownFailure {
            lateinit var coinbase: Coinbase
            lateinit var data: MutableList<Transaction>
            lateinit var header: BlockHeader

            element
                .getElementList("value")
                .asSequence()
                .map {
                    TransactionStorageAdapter.load(
                        ledgerHash, it
                    )
                }.allValues()
                .flatMapSuccess {
                    data = it.toMutableList()
                    CoinbaseStorageAdapter.load(
                        ledgerHash,
                        element.getLinked(
                            "coinbase"
                        )
                    )
                }.flatMapSuccess {
                    coinbase = it
                    BlockHeaderStorageAdapter.load(
                        ledgerHash,
                        element.getLinked(
                            "header"
                        )
                    )
                }.flatMapSuccess {
                    header = it
                    MerkleTreeStorageAdapter.load(
                        ledgerHash,
                        element.getLinked(
                            "merkleTree"
                        )
                    )
                }.mapSuccess {
                    Block(
                        data,
                        coinbase,
                        header,
                        it
                    )
                }
        }
}
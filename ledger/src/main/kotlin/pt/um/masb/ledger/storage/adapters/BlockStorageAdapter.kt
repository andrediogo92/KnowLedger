package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.allValues
import pt.um.masb.common.results.zip
import pt.um.masb.ledger.data.adapters.MerkleTreeStorageAdapter
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Block

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
            )
            setLinked(
                "coinbase", CoinbaseStorageAdapter,
                toStore.coinbase, session
            )
            setLinked(
                "header", BlockHeaderStorageAdapter,
                toStore.header, session
            )
            setLinked(
                "merkleTree", MerkleTreeStorageAdapter,
                toStore.merkleTree, session
            )
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<Block, LoadFailure> =
        tryOrLoadUnknownFailure {
            zip(
                element
                    .getElementList("value")
                    .asSequence()
                    .map {
                        TransactionStorageAdapter.load(
                            ledgerHash, it
                        )
                    }.allValues(),
                CoinbaseStorageAdapter.load(
                    ledgerHash,
                    element.getLinked(
                        "coinbase"
                    )
                ),
                BlockHeaderStorageAdapter.load(
                    ledgerHash,
                    element.getLinked(
                        "header"
                    )
                ),
                MerkleTreeStorageAdapter.load(
                    ledgerHash,
                    element.getLinked(
                        "merkleTree"
                    )
                )
            )
            { data, coinbase, header, merkleTree ->
                Block(
                    data.toMutableList(),
                    coinbase,
                    header,
                    merkleTree
                )
            }
        }
}
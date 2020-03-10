package org.knowledger.ledger.storage.block

import kotlinx.serialization.Serializable
import org.knowledger.collections.SortedList
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.serial.display.BlockSerializer
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction

@Serializable(with = BlockSerializer::class)
interface Block : HashSerializable, Cloneable,
                  Sizeable, LedgerContract {
    val miningReady: Boolean
        get() {
            val blockParams = header.params
            return transactions.size >= blockParams.blockLength / 4 ||
                    approximateSize >= blockParams.blockMemorySize / 2
        }

    val full: Boolean
        get() {
            val blockParams = header.params
            return transactions.size == blockParams.blockLength ||
                    approximateSize >= blockParams.blockMemorySize
        }


    val transactions: SortedList<Transaction>
    val coinbase: Coinbase
    val header: BlockHeader
    val merkleTree: MerkleTree


    fun verifyTransactions(): Boolean

    public override fun clone(): Block

}

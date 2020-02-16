package org.knowledger.ledger.storage.block

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.serial.display.BlockSerializer
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import java.util.*

@Serializable(with = BlockSerializer::class)
interface Block : HashSerializable, Cloneable, Sizeable, LedgerContract {
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


    val transactions: SortedSet<Transaction>
    val coinbase: Coinbase
    val header: BlockHeader
    var merkleTree: MerkleTree

    /**
     * Add a single new transaction.
     *
     * Checks if block is sized correctly.
     *
     * Checks if the transaction is valid.
     *
     * @param transaction   Transaction to attempt to add to the block.
     * @return Whether the transaction was valid and correctly inserted.
     */
    operator fun plus(transaction: Transaction): Boolean

    fun updateHashes()

    fun verifyTransactions(): Boolean

    fun newNonce(): BlockHeader

    public override fun clone(): Block

}

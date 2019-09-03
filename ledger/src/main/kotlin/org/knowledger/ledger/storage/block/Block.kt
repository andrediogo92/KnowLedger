package org.knowledger.ledger.storage.block

import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.core.storage.LedgerContract
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.storage.blockheader.HashedBlockHeader
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.transaction.HashedTransaction
import java.util.*

interface Block : HashSerializable, Cloneable, LedgerContract {
    val data: SortedSet<HashedTransaction>
    val coinbase: HashedCoinbase
    val header: HashedBlockHeader
    var merkleTree: MerkleTree

    /**
     * Add a single new transaction.
     *
     * Checks if block is sized correctly.
     *
     * Checks if the transaction is valid.
     *
     * @param transaction   Transaction to attempt to add to the block.
     * @return Whether the transaction was valid and cprrectly inserted.
     */
    operator fun plus(transaction: HashedTransaction): Boolean

    fun updateHashes()

    fun verifyTransactions(): Boolean

    public override fun clone(): Block
}

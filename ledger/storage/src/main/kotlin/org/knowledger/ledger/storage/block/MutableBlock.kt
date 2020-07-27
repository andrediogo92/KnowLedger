package org.knowledger.ledger.storage.block

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.mutations.Markable

interface MutableBlock : Block, Markable {
    val mutableTransactions: MutableSortedList<MutableTransaction>
    override val blockHeader: MutableBlockHeader
    override val coinbase: MutableCoinbase
    override val merkleTree: MutableMerkleTree

    /**
     * Rebuilds merkle tree with new set of transactions.
     * Sets new merkle tree root in [BlockHeader].
     */
    fun updateHashes()

    /**
     * Sets new extra nonce on [Coinbase].
     */
    fun newExtraNonce()

    /**
     * Recalculates cached transaction size.
     */
    fun recalculateCachedSize()

    /**
     * Add a single new [transaction] to block.
     *
     * Checks if block is not full.
     *
     * Does not check if the [transaction] is valid.
     *
     * @param transaction Pre-verified transaction to add to block.
     * @return Returns whether the transaction was correctly inserted or block is too full.
     */
    operator fun plus(transaction: MutableTransaction): Boolean
}
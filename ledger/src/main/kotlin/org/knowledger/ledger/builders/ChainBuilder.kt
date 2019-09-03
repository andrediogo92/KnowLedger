package org.knowledger.ledger.builders

import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.blockheader.BlockHeader
import org.knowledger.ledger.storage.blockheader.HashedBlockHeader
import org.knowledger.ledger.storage.coinbase.Coinbase
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutput
import java.security.PublicKey
import java.util.*

interface ChainBuilder {
    val chainHash: Hash
    val tag: Tag

    fun block(
        transactions: SortedSet<HashedTransaction>,
        coinbase: HashedCoinbase,
        blockHeader: HashedBlockHeader,
        merkleTree: MerkleTree
    ): Block

    fun blockheader(
        previousHash: Hash,
        merkleRoot: Hash,
        hash: Hash,
        seconds: Long,
        nonce: Long
    ): BlockHeader

    fun coinbase(
        payoutTXO: MutableSet<HashedTransactionOutput>,
        payout: Payout,
        hash: Hash,
        difficulty: Difficulty,
        blockheight: Long
    ): Coinbase

    fun merkletree(
        collapsedTree: MutableList<Hash>,
        levelIndex: MutableList<Int>
    ): MerkleTree

    fun transaction(
        publicKey: PublicKey, physicalData: PhysicalData,
        signature: ByteArray, transactionId: Hash
    ): HashedTransaction

    fun transaction(
        data: PhysicalData
    ): HashedTransaction
}

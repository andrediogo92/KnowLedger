package org.knowledger.ledger.builders

import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.blockheader.HashedBlockHeader
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
    ): HashedBlockHeader

    fun coinbase(
        transactionOutputs: Set<HashedTransactionOutput>,
        payout: Payout, difficulty: Difficulty,
        blockheight: Long, hash: Hash
    ): HashedCoinbase

    fun merkletree(
        collapsedTree: List<Hash>,
        levelIndex: List<Int>
    ): MerkleTree

    fun transactionOutput(
        transactionSet: Set<Hash>,
        prevCoinbase: Hash,
        publicKey: PublicKey, hash: Hash,
        payout: Payout
    ): HashedTransactionOutput

    fun transaction(
        publicKey: PublicKey, physicalData: PhysicalData,
        signature: ByteArray, hash: Hash
    ): HashedTransaction

    fun transaction(
        data: PhysicalData
    ): HashedTransaction
}

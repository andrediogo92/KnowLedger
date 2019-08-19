package org.knowledger.ledger.builders

import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import java.security.PublicKey
import java.util.*

interface ChainBuilder {
    val chainHash: Hash
    val tag: Tag

    fun block(
        transactions: SortedSet<Transaction>,
        coinbase: Coinbase,
        blockHeader: BlockHeader,
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
        payoutTXO: MutableSet<TransactionOutput>,
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
    ): Transaction

    fun transaction(
        data: PhysicalData
    ): Transaction
}

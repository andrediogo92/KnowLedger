package org.knowledger.ledger.builders

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.block.StorageUnawareBlock
import org.knowledger.ledger.storage.blockheader.StorageUnawareBlockHeader
import org.knowledger.ledger.storage.coinbase.StorageUnawareCoinbase
import org.knowledger.ledger.storage.merkletree.StorageUnawareMerkleTree
import java.security.PublicKey
import java.util.*

internal data class WorkingChainBuilder(
    internal val ledgerContainer: LedgerContainer,
    internal val chainId: ChainId,
    internal val identity: Identity
) : ChainBuilder {
    override val chainHash: Hash
        get() = chainId.hashId
    override val tag: Tag
        get() = chainId.tag

    override fun block(
        transactions: SortedSet<Transaction>,
        coinbase: Coinbase,
        blockHeader: BlockHeader,
        merkleTree: MerkleTree
    ): Block =
        StorageUnawareBlock(
            transactions, coinbase,
            blockHeader, merkleTree
        )

    override fun blockheader(
        previousHash: Hash,
        merkleRoot: Hash,
        hash: Hash,
        seconds: Long,
        nonce: Long
    ): BlockHeader =
        StorageUnawareBlockHeader(
            chainId = chainId,
            hasher = ledgerContainer.hasher,
            previousHash = previousHash,
            params = ledgerContainer.ledgerParams.blockParams,
            merkleRoot = merkleRoot,
            hash = hash,
            seconds = seconds,
            nonce = nonce
        )

    override fun coinbase(
        payoutTXO: MutableSet<TransactionOutput>,
        payout: Payout,
        hash: Hash,
        difficulty: Difficulty,
        blockheight: Long
    ): Coinbase =
        StorageUnawareCoinbase(
            payoutTXO = payoutTXO,
            payout = payout,
            hash = hash,
            difficulty = difficulty,
            blockheight = blockheight,
            hasher = ledgerContainer.hasher,
            formula = ledgerContainer.formula,
            coinbaseParams = ledgerContainer.coinbaseParams
        )

    override fun merkletree(
        collapsedTree: MutableList<Hash>,
        levelIndex: MutableList<Int>
    ): MerkleTree =
        StorageUnawareMerkleTree(
            hasher = ledgerContainer.hasher,
            collapsedTree = collapsedTree,
            levelIndex = levelIndex
        )

    override fun transaction(
        publicKey: PublicKey, physicalData: PhysicalData,
        signature: ByteArray, transactionId: Hash
    ): Transaction =
        Transaction(
            chainId = chainId, publicKey = publicKey,
            data = physicalData, signatureInternal = signature,
            hash = transactionId, hasher = ledgerContainer.hasher
        )

    override fun transaction(
        data: PhysicalData
    ): Transaction =
        Transaction(
            chainId = chainId, identity = identity,
            data = data, hasher = ledgerContainer.hasher
        )

}
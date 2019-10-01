package org.knowledger.ledger.builders

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import java.security.PublicKey
import java.util.*

internal data class WorkingChainBuilder(
    internal val ledgerContainer: LedgerContainer,
    internal val chainId: ChainId,
    internal val identity: Identity
) : ChainBuilder {
    override val chainHash: Hash
        get() = chainId.hash
    override val tag: Tag
        get() = chainId.tag

    override fun block(
        transactions: SortedSet<Transaction>,
        coinbase: Coinbase,
        blockHeader: BlockHeader,
        merkleTree: MerkleTree
    ): Block =
        BlockImpl(
            data = transactions,
            coinbase = coinbase,
            header = blockHeader,
            merkleTree = merkleTree
        )

    override fun blockheader(
        previousHash: Hash,
        merkleRoot: Hash,
        hash: Hash,
        seconds: Long,
        nonce: Long
    ): BlockHeader =
        HashedBlockHeaderImpl(
            chainId = chainId,
            previousHash = previousHash,
            blockParams = ledgerContainer.ledgerParams.blockParams,
            merkleRoot = merkleRoot,
            hash = hash,
            seconds = seconds,
            nonce = nonce,
            encoder = ledgerContainer.encoder,
            hasher = ledgerContainer.hasher
        )

    override fun coinbase(
        transactionOutputs: Set<TransactionOutput>,
        payout: Payout, difficulty: Difficulty,
        blockheight: Long, hash: Hash
    ): Coinbase =
        HashedCoinbaseImpl(
            transactionOutputs = transactionOutputs.toMutableSet(),
            payout = payout, difficulty = difficulty,
            blockheight = blockheight,
            coinbaseParams = ledgerContainer.coinbaseParams,
            formula = ledgerContainer.formula, hash = hash,
            hasher = ledgerContainer.hasher,
            encoder = ledgerContainer.encoder
        )

    override fun merkletree(
        collapsedTree: List<Hash>,
        levelIndex: List<Int>
    ): MerkleTree =
        MerkleTreeImpl(
            hasher = ledgerContainer.hasher,
            collapsedTree = collapsedTree,
            levelIndex = levelIndex
        )

    override fun transactionOutput(
        transactionSet: Set<Hash>,
        prevCoinbase: Hash,
        publicKey: PublicKey, hash: Hash,
        payout: Payout
    ): TransactionOutput =
        HashedTransactionOutputImpl(
            previousCoinbase = prevCoinbase,
            publicKey = publicKey, hash = hash,
            payout = payout, transactionSet = transactionSet,
            hasher = ledgerContainer.hasher,
            encoder = ledgerContainer.encoder
        )

    override fun transaction(
        publicKey: PublicKey, physicalData: PhysicalData,
        signature: ByteArray, hash: Hash
    ): Transaction =
        HashedTransactionImpl(
            publicKey = publicKey, hash = hash,
            data = physicalData, signature = signature
        )

    override fun transaction(
        data: PhysicalData
    ): Transaction =
        HashedTransactionImpl(
            identity = identity, data = data,
            hasher = ledgerContainer.hasher,
            encoder = ledgerContainer.encoder
        )

}
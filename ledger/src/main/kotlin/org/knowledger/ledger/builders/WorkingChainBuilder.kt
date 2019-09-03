package org.knowledger.ledger.builders

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.blockheader.BlockHeader
import org.knowledger.ledger.storage.blockheader.HashedBlockHeader
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutput
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
        transactions: SortedSet<HashedTransaction>,
        coinbase: HashedCoinbase,
        blockHeader: HashedBlockHeader,
        merkleTree: MerkleTree
    ): Block =
        BlockImpl(
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
        HashedBlockHeaderImpl(
            chainId = chainId,
            previousHash = previousHash,
            blockParams = ledgerContainer.ledgerParams.blockParams,
            merkleRoot = merkleRoot,
            hash = hash,
            seconds = seconds,
            nonce = nonce,
            cbor = ledgerContainer.cbor,
            hasher = ledgerContainer.hasher
        )

    override fun coinbase(
        payoutTXO: MutableSet<HashedTransactionOutput>,
        payout: Payout,
        hash: Hash,
        difficulty: Difficulty,
        blockheight: Long
    ): HashedCoinbase =
        HashedCoinbaseImpl(
            payoutTXO = payoutTXO,
            payout = payout,
            difficulty = difficulty,
            blockheight = blockheight,
            coinbaseParams = ledgerContainer.coinbaseParams,
            hasher = ledgerContainer.hasher,
            cbor = ledgerContainer.cbor,
            formula = ledgerContainer.formula
        )

    override fun merkletree(
        collapsedTree: MutableList<Hash>,
        levelIndex: MutableList<Int>
    ): MerkleTree =
        MerkleTreeImpl(
            hasher = ledgerContainer.hasher,
            _collapsedTree = collapsedTree,
            _levelIndex = levelIndex
        )

    override fun transaction(
        publicKey: PublicKey, physicalData: PhysicalData,
        signature: ByteArray, hash: Hash
    ): HashedTransaction =
        HashedTransactionImpl(
            publicKey = publicKey, hash = hash,
            data = physicalData, signature = signature,
            hasher = ledgerContainer.hasher,
            cbor = ledgerContainer.cbor
        )

    override fun transaction(
        data: PhysicalData
    ): HashedTransaction =
        HashedTransactionImpl(
            identity = identity, data = data,
            hasher = ledgerContainer.hasher,
            cbor = ledgerContainer.cbor
        )

}
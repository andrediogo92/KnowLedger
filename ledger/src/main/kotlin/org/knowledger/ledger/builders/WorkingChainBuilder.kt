package org.knowledger.ledger.builders

import kotlinx.serialization.PolymorphicSerializer
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.witness.HashedWitnessImpl
import java.security.PublicKey
import org.knowledger.ledger.storage.transaction.output.transactionOutput as transactionOutputBuilder

internal data class WorkingChainBuilder(
    internal val adapterManager: AdapterManager,
    internal val ledgerInfo: LedgerInfo,
    override val chainId: ChainId,
    internal val identity: Identity
) : ChainBuilder {
    override val chainHash: Hash
        get() = chainId.hash
    override val tag: Tag
        get() = chainId.tag

    override fun block(
        transactions: MutableSortedList<Transaction>,
        coinbase: Coinbase,
        blockHeader: BlockHeader,
        merkleTree: MerkleTree
    ): Block =
        BlockImpl(
            _transactions = transactions,
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
            blockParams = ledgerInfo.ledgerParams.blockParams,
            merkleRoot = merkleRoot,
            hash = hash,
            seconds = seconds,
            nonce = nonce,
            encoder = ledgerInfo.encoder,
            hasher = ledgerInfo.hasher
        )

    override fun coinbase(
        witnesses: MutableSortedList<Witness>,
        payout: Payout, difficulty: Difficulty,
        blockheight: Long, extraNonce: Long, hash: Hash
    ): Coinbase =
        HashedCoinbaseImpl(
            witnesses = witnesses,
            payout = payout, difficulty = difficulty,
            blockheight = blockheight,
            ledgerInfo = ledgerInfo,
            extraNonce = extraNonce,
            hash = hash
        )

    override fun merkletree(
        collapsedTree: List<Hash>,
        levelIndex: List<Int>
    ): MerkleTree =
        MerkleTreeImpl(
            collapsedTree = collapsedTree,
            levelIndex = levelIndex,
            hasher = ledgerInfo.hasher
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
            hasher = ledgerInfo.hasher,
            encoder = ledgerInfo.encoder
        )

    override fun transactionOutput(
        payout: Payout,
        newIndex: Int,
        newTransaction: Hash,
        previousBlock: Hash,
        previousIndex: Int,
        previousTransaction: Hash
    ): TransactionOutput =
        transactionOutputBuilder(
            payout = payout,
            newIndex = newIndex,
            newTransaction = newTransaction,
            previousBlock = previousBlock,
            previousIndex = previousIndex,
            previousTransaction = previousTransaction
        )

    override fun witness(
        transactionOutputs: MutableSortedList<TransactionOutput>,
        previousWitnessIndex: Int, prevCoinbase: Hash,
        publicKey: EncodedPublicKey, hash: Hash,
        payout: Payout
    ): Witness =
        HashedWitnessImpl(
            previousWitnessIndex = previousWitnessIndex,
            previousCoinbase = prevCoinbase,
            publicKey = publicKey,
            hash = hash, payout = payout,
            transactionOutputs = transactionOutputs,
            hasher = ledgerInfo.hasher,
            encoder = ledgerInfo.encoder
        )

    override fun data(bytes: ByteArray): LedgerData =
        ledgerInfo.encoder.load(
            adapterManager.findAdapter(tag)!!.serializer,
            bytes
        )

    override fun toBytes(ledgerData: LedgerData): ByteArray =
        ledgerInfo.encoder.dump(
            PolymorphicSerializer(LedgerData::class),
            ledgerData
        )
}
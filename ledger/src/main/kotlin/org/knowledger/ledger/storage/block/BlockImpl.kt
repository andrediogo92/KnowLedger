@file:UseSerializers(
    TransactionByteSerializer::class, MerkleTreeByteSerializer::class,
    BlockHeaderByteSerializer::class, CoinbaseByteSerializer::class
)

package org.knowledger.ledger.storage.block

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.cbor.Cbor
import org.knowledger.collections.SortedList
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.serial.SortedListSerializer
import org.knowledger.ledger.serial.binary.BlockHeaderByteSerializer
import org.knowledger.ledger.serial.binary.CoinbaseByteSerializer
import org.knowledger.ledger.serial.binary.MerkleTreeByteSerializer
import org.knowledger.ledger.serial.binary.TransactionByteSerializer
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.blockheader.StorageAwareBlockHeader
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbase
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTree
import org.tinylog.kotlin.Logger

@Serializable
internal data class BlockImpl(
    @Serializable(with = SortedListSerializer::class)
    override val transactions: SortedList<Transaction>,
    override val coinbase: Coinbase,
    override val header: BlockHeader,
    override var merkleTree: MerkleTree,
    @Transient
    internal var encoder: BinaryFormat = Cbor.plain,
    @Transient
    internal var hasher: Hashers = DEFAULT_HASHER
) : Block, LedgerContract {
    @Transient
    internal var cachedSize: Long? = null

    override val approximateSize: Long
        get() = cachedSize ?: recalculateApproximateSize()

    internal constructor(
        chainId: ChainId, previousHash: Hash,
        params: BlockParams, ledgerInfo: LedgerInfo
    ) : this(
        SortedList(),
        StorageAwareCoinbase(
            ledgerInfo
        ),
        StorageAwareBlockHeader(
            chainId,
            ledgerInfo.hasher,
            ledgerInfo.encoder,
            previousHash,
            params
        ),
        StorageAwareMerkleTree(ledgerInfo.hasher)
    )

    override fun newExtraNonce(): Block {
        coinbase.newNonce()
        merkleTree.buildFromCoinbase(coinbase)
        header.updateMerkleTree(merkleTree.hash)
        return this
    }

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun clone(): Block =
        copy(
            header = header.clone(),
            coinbase = coinbase.clone(),
            merkleTree = merkleTree.clone()
        )

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
    override fun plus(transaction: Transaction): Boolean {
        val transactionSize =
            transaction.approximateSize(encoder)
        val cumSize = approximateSize + transactionSize
        if (cumSize < header.params.blockMemorySize) {
            if (transactions.size < header.params.blockLength) {
                if (transaction.processTransaction(encoder)) {
                    transactions.add(transaction)
                    cachedSize = cumSize
                    Logger.info {
                        "Transaction Successfully added to Block"
                    }
                    return true
                }
            }
        }
        Logger.info {
            "Transaction failed to process. Discarded."
        }
        return false
    }


    override fun markMined(blockheight: Long, difficulty: Difficulty) {
        coinbase.markMined(blockheight, difficulty)
    }


    override fun updateHashes() {
        merkleTree.rebuildMerkleTree(coinbase, transactions.toTypedArray())
        header.updateMerkleTree(merkleTree.hash)
    }


    override fun verifyTransactions(): Boolean {
        return merkleTree.verifyBlockTransactions(
            coinbase,
            transactions.toTypedArray()
        )
    }

    /**
     * Recalculates the entire block size.
     *
     * Is very time consuming and only necessary if:
     *
     * 1. There is a need to calculate the effective block size after deserialization;
     * 2. There is a need to calculate the effective block size after retrieval
     *         of a block from a database.
     */
    fun recalculateApproximateSize(): Long {
        cachedSize = serialize(encoder).size.toLong()
        return cachedSize as Long
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Block) return false

        if (transactions != other.transactions) return false
        if (coinbase != other.coinbase) return false
        if (header != other.header) return false
        if (merkleTree != other.merkleTree) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transactions.hashCode()
        result = 31 * result + coinbase.hashCode()
        result = 31 * result + header.hashCode()
        result = 31 * result + merkleTree.hashCode()
        return result
    }


}
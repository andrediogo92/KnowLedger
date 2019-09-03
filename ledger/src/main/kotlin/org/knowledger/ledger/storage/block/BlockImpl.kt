package org.knowledger.ledger.storage.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.Sizeable
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.storage.LedgerContract
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.serial.SortedSetSerializer
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.storage.blockheader.HashedBlockHeader
import org.knowledger.ledger.storage.blockheader.StorageAwareBlockHeader
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbase
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTree
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.tinylog.kotlin.Logger
import java.util.*

@Serializable
@SerialName("Block")
internal data class BlockImpl(
    @Serializable(with = SortedSetSerializer::class)
    override val data: SortedSet<HashedTransaction>,
    override val coinbase: HashedCoinbase,
    override val header: HashedBlockHeader,
    override var merkleTree: MerkleTree,
    @Transient
    internal var cbor: Cbor = Cbor.plain,
    @Transient
    internal var hasher: Hashers = DEFAULT_HASHER
) : Block, Sizeable, LedgerContract {
    @Transient
    internal var cachedSize: Long? = null

    override val approximateSize: Long
        get() = cachedSize ?: recalculateApproximateSize()

    constructor(
        chainId: ChainId, previousHash: Hash,
        difficulty: Difficulty, blockheight: Long,
        params: BlockParams, ledgerContainer: LedgerContainer
    ) : this(
        sortedSetOf(),
        StorageAwareCoinbase(
            difficulty, blockheight,
            ledgerContainer
        ),
        StorageAwareBlockHeader(
            chainId,
            ledgerContainer.hasher,
            ledgerContainer.cbor,
            previousHash,
            params
        ),
        StorageAwareMerkleTree(ledgerContainer.hasher)
    )

    constructor(
        chainId: ChainId,
        previousHash: Hash,
        difficulty: Difficulty,
        blockheight: Long,
        params: BlockParams
    ) : this(
        chainId, previousHash, difficulty,
        blockheight, params,
        LedgerHandle.getContainer(chainId.ledgerHash)!!
    )

    constructor(
        chainId: ChainId, difficulty: Difficulty,
        previousHash: Hash, coinbaseParams: CoinbaseParams,
        dataFormula: DataFormula, blockheight: Long,
        blockParams: BlockParams, cbor: Cbor, hasher: Hashers
    ) : this(
        sortedSetOf(),
        StorageAwareCoinbase(
            difficulty, blockheight, coinbaseParams,
            dataFormula, cbor, hasher
        ),
        StorageAwareBlockHeader(
            chainId,
            hasher,
            cbor,
            previousHash,
            blockParams
        ),
        StorageAwareMerkleTree(hasher)
    )

    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

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
    override fun plus(transaction: HashedTransaction): Boolean {
        val transactionSize =
            transaction.approximateSize(cbor)
        val cumSize = approximateSize + transactionSize
        if (cumSize < header.params.blockMemSize) {
            if (data.size < header.params.blockLength) {
                if (transaction.processTransaction(cbor)) {
                    data.add(transaction)
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


    override fun updateHashes() {
        merkleTree.rebuildMerkleTree(coinbase, data.toTypedArray())
        header.updateMerkleTree(merkleTree.hash)
    }


    override fun verifyTransactions(): Boolean {
        return merkleTree.verifyBlockTransactions(
            coinbase,
            data.toTypedArray()
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
        cachedSize = serialize(cbor).size.toLong()
        return cachedSize as Long
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockImpl) return false

        if (data != other.data) return false
        if (coinbase != other.coinbase) return false
        if (header != other.header) return false
        if (merkleTree != other.merkleTree) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + coinbase.hashCode()
        result = 31 * result + header.hashCode()
        result = 31 * result + merkleTree.hashCode()
        return result
    }


}
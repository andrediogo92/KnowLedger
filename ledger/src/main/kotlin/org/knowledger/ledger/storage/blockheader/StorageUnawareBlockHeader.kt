package org.knowledger.ledger.storage.blockheader

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.config.LedgerConfiguration
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.storage.BlockHeader
import org.openjdk.jol.info.ClassLayout
import java.time.Instant

@JsonClass(generateAdapter = true)
data class StorageUnawareBlockHeader(
    override val chainId: ChainId,
    @Transient
    override val hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER,
    // Difficulty is fixed at block generation time.
    override val difficulty: Difficulty,
    override val blockheight: Long,
    override val previousHash: Hash,
    override val params: BlockParams,
    override var merkleRoot: Hash = Hash.emptyHash,
    internal var hash: Hash = Hash.emptyHash,
    override var timestamp: Instant = Instant.now(),
    override var nonce: Long = 0
) : BlockHeader {
    override fun updateMerkleTree(newRoot: Hash) {
        updateHash()
        merkleRoot = newRoot
        timestamp = Instant.now()
        nonce = 0
    }


    override val hashId: Hash
        get() = hash

    override val approximateSize: Long =
        ClassLayout
            .parseClass(this::class.java)
            .instanceSize()


    constructor (
        chainId: ChainId, hasher: Hasher,
        previousHash: Hash, difficulty: Difficulty,
        blockheight: Long, blockParams: BlockParams
    ) : this(
        chainId, hasher, difficulty,
        blockheight, previousHash, blockParams
    ) {
        updateHash()
    }

    override fun updateHash() {
        hash = digest(hasher)
    }

    override fun clone(): BlockHeader =
        copy()

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is StorageUnawareBlockHeader)
            return false
        if (chainId != other.chainId)
            return false
        if (difficulty != other.difficulty)
            return false
        if (blockheight != other.blockheight)
            return false
        if (!hashId.contentEquals(
                other.hashId
            )
        )
            return false
        if (!merkleRoot.contentEquals(
                other.merkleRoot
            )
        )
            return false
        if (!previousHash.contentEquals(
                other.previousHash
            )
        )
            return false
        if (timestamp != other.timestamp)
            return false
        if (nonce != other.nonce)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = chainId.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + blockheight.hashCode()
        result = 31 * result + hashId.contentHashCode()
        result = 31 * result + merkleRoot.contentHashCode()
        result = 31 * result + previousHash.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + nonce.hashCode()
        return result
    }
}
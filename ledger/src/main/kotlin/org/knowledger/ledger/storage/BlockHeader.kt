package org.knowledger.ledger.storage

import com.squareup.moshi.JsonClass
import org.knowledger.common.Sizeable
import org.knowledger.common.config.LedgerConfiguration
import org.knowledger.common.data.Difficulty
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hash.Companion.emptyHash
import org.knowledger.common.hash.Hashable
import org.knowledger.common.hash.Hashed
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.bytes
import org.knowledger.common.misc.flattenBytes
import org.knowledger.common.storage.LedgerContract
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.data.MerkleTree
import org.openjdk.jol.info.ClassLayout
import java.time.Instant

@JsonClass(generateAdapter = true)
data class BlockHeader(
    val chainId: ChainId,
    @Transient
    val hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER,
    // Difficulty is fixed at block generation time.
    val difficulty: Difficulty,
    val blockheight: Long,
    internal var hash: Hash,
    var merkleRoot: Hash,
    val previousHash: Hash,
    val params: BlockParams,
    var timestamp: Instant = Instant.now(),
    var nonce: Long = 0
) : Sizeable, Hashed, Hashable,
    LedgerContract {


    override val hashId: Hash
        get() = hash

    override val approximateSize: Long =
        ClassLayout
            .parseClass(this::class.java)
            .instanceSize()


    constructor (
        ledgerId: ChainId, hasher: Hasher,
        previousHash: Hash, difficulty: Difficulty,
        blockheight: Long, blockParams: BlockParams
    ) : this(
        ledgerId, hasher, difficulty,
        blockheight, emptyHash, emptyHash,
        previousHash, blockParams
    ) {
        updateHash()
    }

    /**
     * Hash is a cryptographic digest calculated from previous hashId,
     * internalNonce, internalTimestamp, [MerkleTree]'s root
     * and each [Transaction]'s hashId.
     */
    fun updateHash() {
        hash = digest(hasher)
    }


    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                chainId.hashId.bytes,
                difficulty.difficulty.toByteArray(),
                blockheight.bytes(),
                previousHash.bytes,
                nonce.bytes(),
                timestamp.bytes(),
                merkleRoot.bytes,
                params.blockLength.bytes(),
                params.blockMemSize.bytes()
            )
        )

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is BlockHeader)
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

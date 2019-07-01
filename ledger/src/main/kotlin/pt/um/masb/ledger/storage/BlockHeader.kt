package pt.um.masb.ledger.storage

import com.squareup.moshi.JsonClass
import org.openjdk.jol.info.ClassLayout
import pt.um.masb.common.Sizeable
import pt.um.masb.common.config.LedgerConfiguration
import pt.um.masb.common.data.Difficulty
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.common.hash.Hashable
import pt.um.masb.common.hash.Hashed
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.storage.LedgerContract
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.config.ChainId
import pt.um.masb.ledger.data.MerkleTree
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
) : Sizeable, Hashed, Hashable, LedgerContract {


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

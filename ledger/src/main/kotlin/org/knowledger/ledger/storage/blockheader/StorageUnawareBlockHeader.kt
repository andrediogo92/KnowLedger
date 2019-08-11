package org.knowledger.ledger.storage.blockheader

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.config.LedgerConfiguration
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.storage.BlockHeader
import org.openjdk.jol.info.ClassLayout
import java.time.Instant

@JsonClass(generateAdapter = true)
internal data class StorageUnawareBlockHeader(
    override val chainId: ChainId,
    @Transient
    override val hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER,
    override val previousHash: Hash,
    override val params: BlockParams,
    override var merkleRoot: Hash = Hash.emptyHash,
    internal var hash: Hash = Hash.emptyHash,
    override var seconds: Long = Instant.now().epochSecond,
    override var nonce: Long = 0
) : BlockHeader {
    init {
        if (hash == Hash.emptyHash) {
            updateHash()
        }
    }

    override fun updateMerkleTree(newRoot: Hash) {
        merkleRoot = newRoot
        seconds = Instant.now().epochSecond
        nonce = 0
        updateHash()
    }


    override val hashId: Hash
        get() = hash

    override val approximateSize: Long =
        ClassLayout
            .parseClass(this::class.java)
            .instanceSize()


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
        if (seconds != other.seconds)
            return false
        if (nonce != other.nonce)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = chainId.hashCode()
        result = 31 * result + hashId.contentHashCode()
        result = 31 * result + merkleRoot.contentHashCode()
        result = 31 * result + previousHash.contentHashCode()
        result = 31 * result + seconds.hashCode()
        result = 31 * result + nonce.hashCode()
        return result
    }
}
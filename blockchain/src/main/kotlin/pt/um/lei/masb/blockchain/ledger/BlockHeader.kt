package pt.um.lei.masb.blockchain.ledger

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.Hashable
import java.time.Instant

class BlockHeader(
    val blockChainId: Hash,
    // Difficulty is fixed at block generation time.
    val difficulty: Difficulty,
    val blockheight: Long,
    private var hash: Hash,
    internal var _merkleRoot: Hash,
    val previousHash: Hash,
    internal var _timestamp: Instant = Instant.now(),
    var _nonce: Long = 0
) : Sizeable, Hashable, Storable, BlockChainContract {


    val merkleRoot
        get() = _merkleRoot

    val currentHash
        get() = hash

    val timestamp
        get() = _timestamp

    val nonce
        get() = _nonce

    override val approximateSize: Long =
        ClassLayout
            .parseClass(this::class.java)
            .instanceSize()


    constructor (
        blockChainId: Hash,
        previousHash: Hash,
        difficulty: Difficulty,
        blockheight: Long
    ) : this(
        blockChainId,
        difficulty,
        blockheight,
        emptyHash(),
        emptyHash(),
        previousHash
    )


    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("BlockHeader")
            .apply {
                this.setProperty(
                    "blockChainId",
                    blockChainId
                )
                this.setProperty(
                    "difficulty",
                    difficulty.toByteArray()
                )
                this.setProperty(
                    "blockheight",
                    blockheight
                )
                this.setProperty(
                    "hash",
                    hash
                )
                this.setProperty(
                    "merkleRoot",
                    merkleRoot
                )
                this.setProperty(
                    "previousHash",
                    previousHash
                )
                this.setProperty(
                    "seconds",
                    timestamp.epochSecond
                )
                this.setProperty(
                    "nanos",
                    timestamp.nano
                )
                this.setProperty(
                    "nonce",
                    nonce
                )
            }


    /**
     * Hash is a cryptographic digest calculated from previous hash, _nonce, _timestamp,
     * {@link MerkleTree}'s root and each {@link Transaction}'s hash.
     *
     */
    fun updateHash() {
        hash = digest(crypter)
    }

    fun zeroNonce() {
        _nonce = 0
    }

    fun incNonce() {
        _nonce++
    }

    override fun toString(): String = """
        |   Header: {
        |       BlockChainHash: ${blockChainId.print()}
        |       Difficulty: ${difficulty.print()}
        |       Blockheight: $blockheight
        |       PrevHash: ${previousHash.print()}
        |       Hash: ${hash.print()}
        |       MerkleRoot: ${merkleRoot.print()}
        |       Time: $timestamp
        |   }
        """.trimMargin()

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            ${blockChainId.print()}
            ${difficulty.print()}
            $blockheight
            ${previousHash.print()}
            $nonce
            ${timestamp.epochSecond}
            ${timestamp.nano}
            ${merkleRoot.print()}
            """.trimIndent()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is BlockHeader)
            return false
        if (!blockChainId.contentEquals(
                other.blockChainId
            )
        )
            return false
        if (difficulty != other.difficulty)
            return false
        if (blockheight != other.blockheight)
            return false
        if (!hash.contentEquals(
                other.hash
            )
        )
            return false
        if (!_merkleRoot.contentEquals(
                other._merkleRoot
            )
        )
            return false
        if (!previousHash.contentEquals(
                other.previousHash
            )
        )
            return false
        if (_timestamp != other._timestamp)
            return false
        if (_nonce != other._nonce)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = blockChainId.contentHashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + blockheight.hashCode()
        result = 31 * result + hash.contentHashCode()
        result = 31 * result + _merkleRoot.contentHashCode()
        result = 31 * result + previousHash.contentHashCode()
        result = 31 * result + _timestamp.hashCode()
        result = 31 * result + _nonce.hashCode()
        return result
    }

    companion object : KLogging() {
        val crypter = DEFAULT_CRYPTER
    }
}

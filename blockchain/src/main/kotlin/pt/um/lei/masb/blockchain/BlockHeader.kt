package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.data.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.Hashable
import java.time.Instant

class BlockHeader(
    val blockChainId: BlockChainId,
    // Difficulty is fixed at block generation time.
    val difficulty: Difficulty,
    val blockheight: Long,
    private var hash: Hash,
    internal var _merkleRoot: Hash,
    val previousHash: Hash,
    internal var _timestamp: Instant = Instant.now(),
    private var _nonce: Long = 0
) : Sizeable, Hashable, Storable {


    val merkleRoot
        get() = _merkleRoot

    val currentHash
        get() = hash

    val timestamp
        get() = _timestamp

    val nonce
        get() = _nonce

    override val approximateSize: Long =
        ClassLayout.parseClass(this::class.java)
            .instanceSize()


    constructor (
        blockChainId: BlockChainId,
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


    override fun store(): OElement {
        TODO("store not implemented")
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
        |       $blockChainId
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
            $previousHash
            $nonce
            $timestamp
            $merkleRoot
            """.trimIndent()
        )

    companion object : KLogging() {
        val crypter = DEFAULT_CRYPTER
    }
}

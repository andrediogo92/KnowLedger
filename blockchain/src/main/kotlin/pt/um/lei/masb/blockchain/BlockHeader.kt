package pt.um.lei.masb.blockchain

import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import java.lang.Long.toHexString
import java.math.BigInteger
import java.time.Instant

class BlockHeader(
        val blockChainId: BlockChainId,
        // Difficulty is fixed at block generation time.
        val difficulty: BigInteger,
        val blockheight: Long,
        private var hash: String,
        internal var merkleRoot: String,
        val previousHash: String,
        internal var timestamp: Instant = Instant.now(),
        private var nonce: Long = 0) : Sizeable {

    companion object : KLogging()


    val currentHash get() = hash

    constructor (blockChainId: BlockChainId,
                 previousHash: String,
                 difficulty: BigInteger,
                 blockheight: Long) : this(blockChainId,
                                           difficulty,
                                           blockheight,
                                           "",
                                           "",
                                           previousHash)

    /**
     * Hash is a cryptographic digest calculated from previous hash, nonce, timestamp,
     * {@link MerkleTree}'s root and each {@link Transaction}'s hash.
     *
     */
    fun updateHash() {
        hash = calculateHash()
    }

    internal fun calculateHash(): String =
            DEFAULT_CRYPTER.applyHash(previousHash +
                                      toHexString(nonce) +
                                      timestamp +
                                      merkleRoot)


    fun zeroNonce() {
        nonce = 0
    }

    fun incNonce() {
        nonce++
    }

    override val approximateSize: Long =
            ClassLayout.parseClass(this::class.java)
                .instanceSize()

    override fun toString(): String =
            "Header : [${System.lineSeparator()}difficulty: $difficulty${System.lineSeparator()}prevHash: $previousHash${System.lineSeparator()}Hash: $hash${System.lineSeparator()}Time: $timestamp${System.lineSeparator()}]${System.lineSeparator()}"

}

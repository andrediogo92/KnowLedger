package pt.um.masb.ledger.storage

import com.squareup.moshi.JsonClass
import mu.KLogging
import org.openjdk.jol.info.GraphLayout
import pt.um.masb.common.Sizeable
import pt.um.masb.common.data.Payout
import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.common.hash.Hashable
import pt.um.masb.common.hash.Hashed
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.storage.LedgerContract
import java.math.BigDecimal
import java.security.PublicKey

@JsonClass(generateAdapter = true)
data class TransactionOutput(
    val publicKey: PublicKey,
    val prevCoinbase: Hash,
    override var hashId: Hash,
    var payout: Payout,
    var tx: MutableSet<Hash>
) : Sizeable, Hashed, Hashable,
    LedgerContract {

    override val approximateSize: Long
        get() = GraphLayout
            .parseInstance(this)
            .totalSize()


    constructor(
        publicKey: PublicKey,
        prevCoinbase: Hash,
        cumUTXO: Payout,
        newT: Hash,
        prev: Hash
    ) : this(
        publicKey,
        prevCoinbase,
        emptyHash,
        Payout(BigDecimal.ZERO),
        mutableSetOf<Hash>()
    ) {
        addToPayout(cumUTXO, newT, prev)
    }


    fun addToPayout(
        payout: Payout,
        tx: Hash,
        prev: Hash
    ) {
        this.tx.add(prev + tx)
        this.payout = this.payout.add(payout)
        hashId = digest(crypter)
    }

    /**
     * {@inheritDoc}
     */
    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                tx.sumBy { it.bytes.size },
                tx.asSequence().map { it.bytes },
                publicKey.encoded,
                prevCoinbase.bytes,
                payout.bytes
            )
        )


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionOutput)
            return false

        if (!publicKey.encoded!!.contentEquals(
                other.publicKey.encoded
            )
        )
            return false
        if (!prevCoinbase.contentEquals(
                other.prevCoinbase
            )
        )
            return false
        if (!hashId.contentEquals(other.hashId))
            return false
        if (payout != other.payout)
            return false
        if (!tx.containsAll(other.tx)) return false
        if (tx.size != other.tx.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + prevCoinbase.contentHashCode()
        result = 31 * result + hashId.contentHashCode()
        result = 31 * result + payout.hashCode()
        result = 31 * result + tx.hashCode()
        return result
    }

    companion object : KLogging() {
        val crypter = AvailableHashAlgorithms.SHA256Hasher
    }
}

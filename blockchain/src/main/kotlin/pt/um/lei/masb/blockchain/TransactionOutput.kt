package pt.um.lei.masb.blockchain

import org.openjdk.jol.info.GraphLayout
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.DigestAble
import pt.um.lei.masb.blockchain.utils.getStringFromKey
import java.math.BigDecimal
import java.security.PublicKey

class TransactionOutput(
        val publicKey: PublicKey,
        val prevCoinbase: String,
        private var _hashId: String,
        private var payout: BigDecimal,
        private var tx: MutableSet<String>
) : Sizeable, Hashed, DigestAble {

    override val hashId: String
        get() = _hashId

    override val approximateSize: Long =
            GraphLayout.parseInstance(this)
                .totalSize()

    val payoutTX: BigDecimal
        get() = payout

    val txSet: Set<String>
        get() = tx


    constructor(publicKey: PublicKey,
                hashId: String,
                cumUTXO: BigDecimal,
                newT: String,
                prev: String) : this(publicKey,
                                     "",
                                     hashId,
                                     BigDecimal("0"),
                                     mutableSetOf<String>()) {
        addToPayout(cumUTXO, newT, prev)
    }

    private fun recalculateHash(): String =
            DEFAULT_CRYPTER.applyHash("${tx.joinToString("") { it }}${getStringFromKey(publicKey)}")


    fun addToPayout(payout: BigDecimal,
                    tx: String,
                    prev: String) {
        this.tx.add(prev + tx)
        this.payout = this.payout.add(payout)
        _hashId = recalculateHash()
    }



    /**
     * {@inheritDoc}
     */
    override fun digest(c: Crypter): String =
            c.applyHash("$publicKey$prevCoinbase$hashId$payout${tx.joinToString("") { it }}")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionOutput) return false

        if (publicKey != other.publicKey) return false
        if (prevCoinbase != other.prevCoinbase) return false
        if (hashId != other.hashId) return false
        if (payout != other.payout) return false
        if (tx != other.tx) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + prevCoinbase.hashCode()
        result = 31 * result + hashId.hashCode()
        result = 31 * result + payout.hashCode()
        result = 31 * result + tx.hashCode()
        return result
    }

    override fun toString(): String =
            "TransactionOutput(publicKey=$publicKey, prevCoinbase='$prevCoinbase', hashId='$hashId', payout=$payout, tx=$tx)"


}

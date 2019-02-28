package pt.um.lei.masb.blockchain.ledger

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.openjdk.jol.info.GraphLayout
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.Hashable
import java.math.BigDecimal
import java.security.PublicKey

data class TransactionOutput(
    val publicKey: PublicKey,
    val prevCoinbase: Hash,
    private var _hashId: Hash,
    private var payout: Payout,
    private var tx: MutableSet<Hash>
) : Sizeable, Hashed, Hashable, Storable,
    BlockChainContract {


    override val hashId: Hash
        get() = _hashId

    override val approximateSize: Long
        get() = GraphLayout
            .parseInstance(this)
            .totalSize()

    val payoutTX: Payout
        get() = payout

    val txSet: Set<Hash>
        get() = tx


    constructor(
        publicKey: PublicKey,
        prevCoinbase: Hash,
        cumUTXO: Payout,
        newT: Hash,
        prev: Hash
    ) : this(
        publicKey,
        prevCoinbase,
        emptyHash(),
        BigDecimal("0"),
        mutableSetOf<Hash>()
    ) {
        addToPayout(cumUTXO, newT, prev)
    }

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("TransactionOutput")
            .apply {
                this.setProperty(
                    "publicKey",
                    publicKey.encoded
                )
                this.setProperty(
                    "prevCoinbase",
                    prevCoinbase
                )
                this.setProperty(
                    "hashId",
                    hashId
                )
                this.setProperty(
                    "payout",
                    payout
                )
                this.setProperty(
                    "txSet",
                    txSet
                )
            }

    fun addToPayout(
        payout: Payout,
        tx: Hash,
        prev: Hash
    ) {
        this.tx.add(prev + tx)
        this.payout = this.payout.add(payout)
        _hashId = digest(crypter)
    }

    /**
     * {@inheritDoc}
     */
    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
                ${publicKey.encoded.print()}
                ${prevCoinbase.print()}
                ${hashId.print()}
                $payout
                ${tx.joinToString("") { it.print() }}
                """.trimIndent()
        )

    override fun toString(): String =
        StringBuilder().let { sb: StringBuilder
            ->
            sb.append(
                """
                |           TransactionOutput: {
                |               PublicKey: ${publicKey.encoded.print()},
                |               PrevCoinbase: ${prevCoinbase.print()},
                |               HashId: ${hashId.print()},
                |               Payout: $payout,
                |               TXs: [
                """.trimMargin()
            )
            txSet.forEach {
                sb.append(
                    """
                    |                   ${it.print()},
                    """.trimIndent()
                )
            }
            sb.append(
                """
                |               ]
                |           }
                """.trimMargin()
            )
            sb.toString()
        }

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
        if (!_hashId.contentEquals(other._hashId))
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
        result = 31 * result + _hashId.contentHashCode()
        result = 31 * result + payout.hashCode()
        result = 31 * result + tx.hashCode()
        return result
    }

    companion object : KLogging() {
        val crypter = DEFAULT_CRYPTER
    }
}

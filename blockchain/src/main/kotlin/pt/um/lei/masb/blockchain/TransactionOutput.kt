package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.openjdk.jol.info.GraphLayout
import pt.um.lei.masb.blockchain.data.Storable
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
) : Sizeable, Hashed, Hashable, Storable {


    override val hashId: Hash
        get() = _hashId

    override val approximateSize: Long =
        GraphLayout.parseInstance(this)
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
        "",
        BigDecimal("0"),
        mutableSetOf<Hash>()
    ) {
        addToPayout(cumUTXO, newT, prev)
        _hashId = digest(crypter)
    }

    override fun store(): OElement {
        TODO("not implemented")
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
                $publicKey
                $prevCoinbase
                $hashId
                $payout
                ${tx.joinToString("") { it }}
                """.trimIndent()
        )

    override fun toString(): String =
        StringBuilder().let { sb: StringBuilder
            ->
            sb.append(
                """
                |           TransactionOutput: {
                |               PublicKey: $publicKey,
                |               PrevCoinbase: $prevCoinbase,
                |               HashId: $hashId,
                |               Payout: $payout,
                |               TXs: [
                """.trimMargin()
            )
            txSet.forEach {
                sb.append(
                    """
                    |                   $it
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

    companion object : KLogging() {
        val crypter = DEFAULT_CRYPTER
    }
}

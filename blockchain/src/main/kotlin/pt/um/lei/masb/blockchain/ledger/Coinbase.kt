package pt.um.lei.masb.blockchain.ledger

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.data.DataFormula
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.data.calculateDiff
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.Hashable
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.security.PublicKey
import java.time.temporal.ChronoField

/**
 * The coinbase transaction. Pays out to contributors to the blockchain.
 *
 * The coinbase will be continually updated
 * to reflect changes to the block.
 */
class Coinbase(
    private val _payoutTXO: MutableSet<TransactionOutput>,
    private var coinbase: Payout,
    private var _hashId: Hash,
    @Transient
    private val payoutFormula: DataFormula
) : Sizeable, Hashed, Hashable, Storable,
    BlockChainContract {


    val payoutTXO: Set<TransactionOutput>
        get() = _payoutTXO

    override val hashId: Hash
        get() = _hashId

    val coinbasePayout: Payout
        get() = coinbase

    override val approximateSize: Long
        get() {
            val sum = payoutTXO
                .fold(0) { acc: Long,
                           tOut: TransactionOutput ->
                    acc + tOut.approximateSize
                }
            return sum + ClassLayout
                .parseClass(this::class.java)
                .instanceSize()
        }

    init {
        if (_hashId.contentEquals(
                emptyHash()
            )
        ) {
            _hashId = digest(crypter)
        }
    }

    constructor() : this(
        mutableSetOf(),
        BigDecimal.ZERO,
        emptyHash(),
        ::calculateDiff
    )

    constructor(
        payoutTXO: MutableSet<TransactionOutput>,
        coinbase: Payout,
        hashId: Hash
    ) : this(
        payoutTXO,
        coinbase,
        hashId,
        ::calculateDiff
    )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Coinbase")
            .apply {
                this.setProperty(
                    "payoutTXOs",
                    payoutTXO.map {
                        it.store(session)
                    })
                this.setProperty(
                    "coinbase",
                    coinbase
                )
                this.setProperty(
                    "hashId",
                    hashId
                )
            }

    /**
     * Takes the new Transaction and attempts to calculate a fluctuation from
     * the previous Transaction of same type and in the same geographical area.
     *
     * Adds a payout for the transaction's agent in a transaction output.
     * @param newT                  Transaction to contribute to payout.
     * @param latestKnown           Transaction to compare for fluctuation.
     * @param latestUTXO            Last unspent transaction output for
     *                              the new Transaction's publisher.
     *                              <pw>
     *                              If it's the first time for this identity, supply
     *                              null.
     */
    internal fun addToInput(
        newT: Transaction,
        latestKnown: Transaction?,
        latestUTXO: TransactionOutput?
    ) {
        val payout: Payout
        val lkHash: Hash
        val lUTXOHash: Hash = latestUTXO?.hashId
            ?: emptyHash()

        //None are known for this area.
        if (latestKnown == null) {
            payout = payoutFormula(
                BASE,
                TIME_BASE,
                BigDecimal.ONE,
                VALUE_BASE,
                BigDecimal.ONE,
                newT.data.dataConstant,
                THRESHOLD,
                MATH_CONTEXT
            )
            lkHash = emptyHash()
        } else {
            payout = calculatePayout(
                newT.data,
                latestKnown.data
            )
            lkHash = latestKnown.hashId
        }
        coinbase = coinbase.add(payout)
        addToOutputs(
            newT.publicKey,
            lUTXOHash,
            newT.hashId,
            lkHash,
            payout
        )
        _hashId = digest(crypter)
    }

    private fun getTimeDelta(
        dt: PhysicalData,
        dt2: PhysicalData
    ): BigDecimal {
        val stamp1 = BigDecimal(
            dt.instant.epochSecond * 1000000000 +
                    dt.instant.get(ChronoField.NANO_OF_SECOND)
        )
        val stamp2 = BigDecimal(
            dt2.instant.epochSecond * 1000000000 +
                    dt2.instant.get(ChronoField.NANO_OF_SECOND)
        )
        return stamp1
            .subtract(stamp2)
            .divide(stamp1, MATH_CONTEXT)
    }

    /**
     * @param publicKey Public Key of transaction publisher.
     * @param prevUTXO  Previous known UTXO's hash.
     * @param newT      Transaction to contribute to payout's hash.
     * @param prev      Transaction compared for fluctuation's hash,
     *                  might be empty.
     * @param payout    Payout amount to publisher.
     */
    private fun addToOutputs(
        publicKey: PublicKey,
        prevUTXO: Hash,
        newT: Hash,
        prev: Hash,
        payout: Payout
    ) {
        _payoutTXO
            .firstOrNull { it.publicKey == publicKey }
            .let {
                it?.addToPayout(
                    payout,
                    newT,
                    prev
                )
                    ?: _payoutTXO.add(
                        TransactionOutput(
                            publicKey,
                            prevUTXO,
                            payout,
                            newT,
                            prev
                        )
                    )
            }
    }

    private fun calculatePayout(
        dt: PhysicalData,
        dt2: PhysicalData
    ): Payout =
        payoutFormula(
            BASE,
            TIME_BASE,
            getTimeDelta(dt, dt2),
            VALUE_BASE,
            dt.calculateDiff(dt2.data),
            dt.dataConstant,
            THRESHOLD,
            MATH_CONTEXT
        )

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            $coinbase
            ${payoutTXO.joinToString("") {
                it.hashId.print()
            }}
        """.trimIndent()
        )

    override fun toString(): String =
        StringBuilder().let { sb: StringBuilder
            ->
            sb.append(
                """
                |   Coinbase: {
                |       Total: $coinbasePayout
                |       Hash: ${hashId.print()}
                |       Payouts: [
                """.trimMargin()
            )
            payoutTXO.forEach {
                sb.append(
                    """$it,
                    |
                    """.trimMargin()
                )
            }
            sb.append(
                """
                |       ]
                |   }
                """.trimMargin()
            )
            sb.toString()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Coinbase) return false

        if (!_payoutTXO.containsAll(other._payoutTXO)) return false
        if (_payoutTXO.size != other._payoutTXO.size) return false
        if (coinbase != other.coinbase) return false
        if (!_hashId.contentEquals(other._hashId)) return false
        if (payoutFormula != other.payoutFormula) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _payoutTXO.hashCode()
        result = 31 * result + coinbase.hashCode()
        result = 31 * result + _hashId.contentHashCode()
        result = 31 * result + payoutFormula.hashCode()
        return result
    }

    companion object : KLogging() {
        const val TIME_BASE = 5
        const val VALUE_BASE = 2
        const val BASE = 3
        const val THRESHOLD = 100000
        const val OTHER = 50
        const val DATA = 5
        val MATH_CONTEXT = MathContext(
            12,
            RoundingMode.HALF_EVEN
        )
        val crypter = DEFAULT_CRYPTER
    }

}

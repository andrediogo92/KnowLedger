package pt.um.masb.ledger.storage

import com.squareup.moshi.JsonClass
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.masb.common.Sizeable
import pt.um.masb.common.data.DataFormula
import pt.um.masb.common.data.Payout
import pt.um.masb.common.data.calculateDiff
import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.common.hash.Hashable
import pt.um.masb.common.hash.Hashed
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.storage.LedgerContract
import pt.um.masb.ledger.data.PhysicalData
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.security.PublicKey
import java.time.temporal.ChronoField

/**
 * The coinbase transaction. Pays out to contributors to
 * the ledger.
 *
 * The coinbase will be continually updated to reflect
 * changes to the block.
 */
@JsonClass(generateAdapter = true)
data class Coinbase(
    val payoutTXO: MutableSet<TransactionOutput>,
    var coinbase: Payout,
    override var hashId: Hash,
    @Transient
    private val payoutFormula: DataFormula = ::calculateDiff
) : Sizeable, Hashed, Hashable, LedgerContract {


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
        if (hashId.contentEquals(
                emptyHash
            )
        ) {
            hashId = digest(crypter)
        }
    }

    constructor() : this(
        mutableSetOf(),
        Payout(BigDecimal.ZERO),
        emptyHash,
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

    /**
     * Takes the [newTransaction] and attempts to calculate a
     * fluctuation from the [latestKnown] of the same type
     * and in the same geographical area.
     *
     * Uses the [latestUTXO] for the new [Transaction]'s publisher.
     *
     * Adds a [Payout] for the transaction's agent in a transaction
     * output.
     *
     * There may not be a [latestKnown], in which case the [newTransaction]
     * is treated as the first known [Transaction] of that type.
     *
     * There may not be a [latestUTXO], in which case the first
     * transaction output must be created for the [Identity] which
     * supplied the [newTransaction].
     */
    internal fun addToInput(
        newTransaction: Transaction,
        latestKnown: Transaction?,
        latestUTXO: TransactionOutput?
    ) {
        val payout: Payout
        val lkHash: Hash
        val lUTXOHash: Hash = latestUTXO?.hashId
            ?: emptyHash

        //None are known for this area.
        if (latestKnown == null) {
            payout = payoutFormula(
                BASE,
                TIME_BASE,
                BigDecimal.ONE,
                VALUE_BASE,
                BigDecimal.ONE,
                newTransaction.data.dataConstant,
                THRESHOLD,
                MATH_CONTEXT
            )
            lkHash = emptyHash
        } else {
            payout = calculatePayout(
                newTransaction.data,
                latestKnown.data
            )
            lkHash = latestKnown.hashId
        }
        coinbase = coinbase.add(payout)
        addToOutputs(
            newTransaction.publicKey,
            lUTXOHash,
            newTransaction.hashId,
            lkHash,
            payout
        )
        hashId = digest(crypter)
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
     * Adds a [payout] to a transaction output in the [publicKey]'s
     * owner's behalf.
     *
     * If a [TransactionOutput] for this same [PublicKey] representing
     * an active participant already exists, appends the pair consisting
     * of [newTransaction] and [previousTransaction] to the set of
     * transactions counted into the calculation of the total payout to
     * this participant.
     * The respective [payout] associated with this transaction is
     * added to the total for this [TransactionOutput].
     *
     * If a [TransactionOutput] does not yet exist, a new [TransactionOutput]
     * is created referencing the [previousUTXO].
     */
    private fun addToOutputs(
        publicKey: PublicKey,
        previousUTXO: Hash,
        newTransaction: Hash,
        previousTransaction: Hash,
        payout: Payout
    ) {
        payoutTXO
            .firstOrNull { it.publicKey == publicKey }
            ?.addToPayout(
                payout,
                newTransaction,
                previousTransaction
            )
            ?: payoutTXO.add(
                TransactionOutput(
                    publicKey,
                    previousUTXO,
                    payout,
                    newTransaction,
                    previousTransaction
                )
            )

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

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                payoutTXO.sumBy {
                    it.hashId.bytes.size
                },
                payoutTXO.asSequence().map {
                    it.hashId.bytes
                },
                coinbase.bytes
            )
        )


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Coinbase) return false

        if (!payoutTXO.containsAll(other.payoutTXO)) return false
        if (payoutTXO.size != other.payoutTXO.size) return false
        if (coinbase != other.coinbase) return false
        if (!hashId.contentEquals(other.hashId)) return false
        if (payoutFormula != other.payoutFormula) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payoutTXO.hashCode()
        result = 31 * result + coinbase.hashCode()
        result = 31 * result + hashId.contentHashCode()
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
        val crypter = AvailableHashAlgorithms.SHA256Hasher
    }

}


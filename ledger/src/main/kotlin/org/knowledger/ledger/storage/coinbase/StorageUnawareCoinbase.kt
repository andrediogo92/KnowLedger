package org.knowledger.ledger.storage.coinbase

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.config.LedgerConfiguration
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.openjdk.jol.info.ClassLayout
import java.math.BigDecimal
import java.security.PublicKey
import java.time.temporal.ChronoField


@JsonClass(generateAdapter = true)
data class StorageUnawareCoinbase(
    override val payoutTXO: MutableSet<TransactionOutput>,
    override var payout: Payout,
    internal var hash: Hash,
    @Transient
    override val hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER,
    @Transient
    override val formula: DataFormula = DefaultDiff,
    @Transient
    override val coinbaseParams: CoinbaseParams = CoinbaseParams()
) : Coinbase {
    override fun clone(): Coinbase =
        copy()


    override val hashId: Hash
        get() = hash

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

    internal constructor(
        container: LedgerContainer
    ) : this(
        mutableSetOf(),
        Payout(BigDecimal.ZERO),
        Hash.emptyHash,
        container.hasher,
        container.formula,
        container.coinbaseParams
    ) {
        hash = digest(container.hasher)
    }

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
    override fun addToInput(
        newTransaction: Transaction,
        latestKnown: Transaction?,
        latestUTXO: TransactionOutput?
    ) {
        val payout: Payout
        val lkHash: Hash
        val lUTXOHash: Hash = latestUTXO?.hashId
            ?: Hash.emptyHash

        //None are known for this area.
        if (latestKnown == null) {
            payout = formula.calculateDiff(
                coinbaseParams.baseIncentive,
                coinbaseParams.timeIncentive,
                BigDecimal.ONE,
                coinbaseParams.valueIncentive,
                BigDecimal.ONE,
                newTransaction.data.dataConstant,
                coinbaseParams.dividingThreshold,
                PhysicalData.MATH_CONTEXT
            )
            lkHash = Hash.emptyHash
        } else {
            payout = calculatePayout(
                newTransaction.data,
                latestKnown.data,
                formula,
                coinbaseParams
            )
            lkHash = latestKnown.hashId
        }
        this.payout = this.payout.add(payout)
        addToOutputs(
            newTransaction.publicKey,
            lUTXOHash,
            newTransaction.hashId,
            lkHash,
            payout
        )
        hash = digest(hasher)
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
            .divide(stamp1, PhysicalData.MATH_CONTEXT)
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
                    previousTransaction,
                    hasher
                )
            )

    }

    private fun calculatePayout(
        dt: PhysicalData,
        dt2: PhysicalData,
        payoutFormula: DataFormula,
        coinbaseParams: CoinbaseParams
    ): Payout =
        payoutFormula.calculateDiff(
            coinbaseParams.baseIncentive,
            coinbaseParams.timeIncentive,
            getTimeDelta(dt, dt2),
            coinbaseParams.valueIncentive,
            dt.calculateDiff(dt2.data),
            dt.dataConstant,
            coinbaseParams.dividingThreshold,
            PhysicalData.MATH_CONTEXT
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StorageUnawareCoinbase) return false

        if (payoutTXO.size != other.payoutTXO.size) return false
        if (payoutTXO
                .asSequence()
                .zip(other.payoutTXO.asSequence())
                .any { (tx1, tx2) ->
                    tx1 != tx2
                }
        ) return false
        if (!payout.contentEquals(other.payout)) return false
        if (!hashId.contentEquals(other.hashId)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payoutTXO.hashCode()
        result = 31 * result + payout.hashCode()
        result = 31 * result + hashId.contentHashCode()
        return result
    }

}


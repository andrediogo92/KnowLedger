package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutput
import java.math.BigDecimal
import java.time.temporal.ChronoField


@Serializable
@SerialName("Coinbase")
internal data class CoinbaseImpl(
    @SerialName("transactionOutputs")
    internal var _transactionOutputs: MutableSet<HashedTransactionOutput>,
    override var payout: Payout,
    // Difficulty is fixed at block generation time.
    override val difficulty: Difficulty,
    override var blockHeight: Long,
    @SerialName("coinbaseParams")
    override val coinbaseParams: CoinbaseParams,
    @Transient
    override val formula: DataFormula = DefaultDiff
) : Coinbase {
    override val transactionOutputs: Set<HashedTransactionOutput>
        get() = _transactionOutputs


    internal constructor(
        difficulty: Difficulty,
        blockheight: Long,
        container: LedgerContainer
    ) : this(
        mutableSetOf(),
        Payout(BigDecimal.ZERO),
        difficulty,
        blockheight,
        container.coinbaseParams,
        container.formula
    )

    internal fun getTimeDelta(
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
            .divide(stamp1, GLOBALCONTEXT)
    }

    internal fun calculatePayout(
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
            GLOBALCONTEXT
        )

    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

    override fun clone(): Coinbase =
        copy(
            _transactionOutputs =
            transactionOutputs
                .asSequence()
                .toMutableSet()
        )


}


@file:UseSerializers(TransactionOutputByteSerializer::class, CoinbaseParamsByteSerializer::class)
package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.misc.copyMutableSet
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.serial.internal.CoinbaseParamsByteSerializer
import org.knowledger.ledger.serial.internal.TransactionOutputByteSerializer
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.storage.TransactionOutput
import java.math.BigDecimal
import java.time.temporal.ChronoField

@Serializable
internal data class CoinbaseImpl(
    internal var _transactionOutputs: MutableSet<TransactionOutput>,
    override var payout: Payout,
    // Difficulty is fixed at block generation time.
    override val difficulty: Difficulty,
    override var blockheight: Long,
    override var extraNonce: Long = 0,
    override val coinbaseParams: CoinbaseParams,
    @Transient
    override val formula: DataFormula = DefaultDiff
) : Coinbase {
    override val transactionOutputs: Set<TransactionOutput>
        get() = _transactionOutputs


    internal constructor(
        difficulty: Difficulty,
        blockheight: Long,
        container: LedgerContainer
    ) : this(
        _transactionOutputs = mutableSetOf(),
        payout = Payout(BigDecimal.ZERO),
        difficulty = difficulty,
        blockheight = blockheight,
        coinbaseParams = container.coinbaseParams,
        formula = container.formula
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

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun clone(): CoinbaseImpl =
        copy(
            _transactionOutputs =
            transactionOutputs.copyMutableSet(TransactionOutput::clone)
        )


}


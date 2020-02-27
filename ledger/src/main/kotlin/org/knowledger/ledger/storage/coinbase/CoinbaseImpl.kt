@file:UseSerializers(
    TransactionOutputByteSerializer::class,
    CoinbaseParamsByteSerializer::class,
    DifficultySerializer::class,
    PayoutSerializer::class
)
package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import org.knowledger.collections.copyMutableSet
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import org.knowledger.ledger.core.base.data.DefaultDiff
import org.knowledger.ledger.core.serial.DifficultySerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.serial.binary.CoinbaseParamsByteSerializer
import org.knowledger.ledger.serial.binary.TransactionOutputByteSerializer
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import java.math.BigDecimal
import java.time.temporal.ChronoField

@Serializable
internal data class CoinbaseImpl(
    private var _transactionOutputs: MutableSet<TransactionOutput>,
    private var _payout: Payout,
    override val coinbaseParams: CoinbaseParams,
    // Difficulty is fixed at mining time.
    private var _difficulty: Difficulty = Difficulty.MAX_DIFFICULTY,
    private var _blockheight: Long = -1,
    private var _extraNonce: Long = 0,
    @Transient
    override val formula: DataFormula = DefaultDiff
) : Coinbase {
    override val transactionOutputs: Set<TransactionOutput>
        get() = _transactionOutputs

    override val blockheight: Long
        get() = _blockheight

    override val difficulty: Difficulty
        get() = _difficulty

    override val payout: Payout
        get() = _payout

    override val extraNonce: Long
        get() = _extraNonce

    internal constructor(
        info: LedgerInfo
    ) : this(
        _transactionOutputs = mutableSetOf(),
        _payout = Payout(BigDecimal.ZERO),
        coinbaseParams = info.coinbaseParams,
        formula = info.formula
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

    override fun markMined(blockheight: Long, difficulty: Difficulty) {
        this._blockheight = blockheight
        this._difficulty = difficulty
    }

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun clone(): CoinbaseImpl =
        copy(
            _transactionOutputs =
            transactionOutputs.copyMutableSet(TransactionOutput::clone)
        )

    override fun newNonce() {
        _extraNonce++
    }


    internal fun newTXO(transactionOutput: HashedTransactionOutputImpl) {
        _transactionOutputs.add(transactionOutput)
    }

    internal fun updatePayout(payoutToAdd: Payout) {
        _payout += payoutToAdd
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Coinbase) return false

        if (_transactionOutputs != other.transactionOutputs) return false
        if (_payout != other.payout) return false
        if (_difficulty != other.difficulty) return false
        if (_blockheight != other.blockheight) return false
        if (_extraNonce != other.extraNonce) return false
        if (coinbaseParams != other.coinbaseParams) return false
        if (formula != other.formula) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _transactionOutputs.hashCode()
        result = 31 * result + _payout.hashCode()
        result = 31 * result + _difficulty.hashCode()
        result = 31 * result + _blockheight.hashCode()
        result = 31 * result + _extraNonce.hashCode()
        result = 31 * result + coinbaseParams.hashCode()
        result = 31 * result + formula.hashCode()
        return result
    }

}


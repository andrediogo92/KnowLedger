@file:UseSerializers(
    WitnessByteSerializer::class,
    CoinbaseParamsByteSerializer::class,
    DifficultySerializer::class,
    PayoutSerializer::class
)

package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.collections.copyMutableSortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import org.knowledger.ledger.core.base.data.DefaultDiff
import org.knowledger.ledger.core.serial.DifficultySerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.serial.MutableSortedListSerializer
import org.knowledger.ledger.serial.binary.CoinbaseParamsByteSerializer
import org.knowledger.ledger.serial.binary.WitnessByteSerializer
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.storage.Markable
import org.knowledger.ledger.storage.NonceRegen
import org.knowledger.ledger.storage.Witness
import java.math.BigDecimal

@Serializable
internal data class CoinbaseImpl(
    override val coinbaseParams: CoinbaseParams,
    @Serializable(with = MutableSortedListSerializer::class)
    private var _witnesses: MutableSortedList<Witness> = mutableSortedListOf(),
    private var _payout: Payout = Payout.ZERO,
    // Difficulty is fixed at mining time.
    private var _difficulty: Difficulty = Difficulty.MAX_DIFFICULTY,
    private var _blockheight: Long = -1,
    private var _extraNonce: Long = 0,
    @Transient
    override val formula: DataFormula = DefaultDiff
) : Coinbase, NonceRegen, Markable {
    override val witnesses: SortedList<Witness>
        get() = _witnesses

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
        _witnesses = mutableSortedListOf(),
        _payout = Payout(BigDecimal.ZERO),
        coinbaseParams = info.coinbaseParams,
        formula = info.formula
    )

    private fun getTimeDelta(
        dt: PhysicalData, dt2: PhysicalData
    ): BigDecimal {
        val stamp1 = BigDecimal(dt.millis)
        val stamp2 = BigDecimal(dt2.millis)
        return stamp1
            .subtract(stamp2)
            .divide(stamp1, GLOBALCONTEXT)
    }

    internal fun calculatePayout(
        dt: PhysicalData, dt2: PhysicalData
    ): Payout =
        formula.calculateDiff(
            coinbaseParams.baseIncentive,
            coinbaseParams.timeIncentive,
            getTimeDelta(dt, dt2),
            coinbaseParams.valueIncentive,
            dt.calculateDiff(dt2.data),
            dt.dataConstant,
            coinbaseParams.dividingThreshold,
            GLOBALCONTEXT
        )

    internal fun calculatePayout(dt: PhysicalData): Payout =
        formula.calculateDiff(
            coinbaseParams.baseIncentive,
            coinbaseParams.timeIncentive,
            BigDecimal.ONE,
            coinbaseParams.valueIncentive,
            BigDecimal.ONE,
            dt.data.dataConstant,
            coinbaseParams.dividingThreshold,
            GLOBALCONTEXT
        )

    override fun markForMining(
        blockheight: Long, difficulty: Difficulty
    ) {
        _blockheight = blockheight
        _difficulty = difficulty
    }

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun clone(): CoinbaseImpl =
        copy(
            _witnesses =
            witnesses.copyMutableSortedList(Witness::clone)
        )

    override fun newNonce() {
        _extraNonce++
    }


    internal fun newTXO(witness: Witness) {
        _witnesses.add(witness)
    }

    internal fun updatePayout(payoutToAdd: Payout) {
        _payout += payoutToAdd
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Coinbase) return false

        if (witnesses != other.witnesses) return false
        if (payout != other.payout) return false
        if (difficulty != other.difficulty) return false
        if (blockheight != other.blockheight) return false
        if (extraNonce != other.extraNonce) return false
        if (coinbaseParams != other.coinbaseParams) return false
        if (formula != other.formula) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _witnesses.hashCode()
        result = 31 * result + _payout.hashCode()
        result = 31 * result + _difficulty.hashCode()
        result = 31 * result + _blockheight.hashCode()
        result = 31 * result + _extraNonce.hashCode()
        result = 31 * result + coinbaseParams.hashCode()
        result = 31 * result + formula.hashCode()
        return result
    }

}


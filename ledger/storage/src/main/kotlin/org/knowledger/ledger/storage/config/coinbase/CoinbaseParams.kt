package org.knowledger.ledger.storage.config.coinbase

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.DataFormula
import org.knowledger.ledger.storage.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.config.GlobalLedgerConfiguration
import java.math.BigDecimal

interface CoinbaseParams : HashSerializable, LedgerContract {
    val hashSize: Int
    val timeIncentive: Long
    val valueIncentive: Long
    val baseIncentive: Long
    val dividingThreshold: Long
    val formula: Hash

    private fun getTimeDelta(dt: PhysicalData, dt2: PhysicalData): BigDecimal {
        val stamp1 = BigDecimal(dt.millis)
        val stamp2 = BigDecimal(dt2.millis)
        return stamp1.subtract(stamp2).divide(stamp1, GlobalLedgerConfiguration.GLOBALCONTEXT)
    }


    fun calculatePayout(dt: PhysicalData, dt2: PhysicalData, formula: DataFormula): Payout =
        formula.calculateDiff(
            baseIncentive, timeIncentive, getTimeDelta(dt, dt2), valueIncentive,
            dt.calculateDiff(dt2.data), dt.dataConstant, dividingThreshold,
            GlobalLedgerConfiguration.GLOBALCONTEXT
        )

    fun calculatePayout(dt: PhysicalData, formula: DataFormula): Payout =
        formula.calculateDiff(
            baseIncentive, timeIncentive, BigDecimal.ONE, valueIncentive, BigDecimal.ONE,
            dt.data.dataConstant, dividingThreshold, GlobalLedgerConfiguration.GLOBALCONTEXT
        )
}
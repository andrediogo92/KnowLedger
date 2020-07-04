package org.knowledger.ledger.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.base.hash.classDigest
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers.SHA3512Hasher
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.DefaultDiff
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.service.ServiceClass
import java.math.BigDecimal

@Serializable
data class CoinbaseParams(
    val hashSize: Int,
    val timeIncentive: Long = 5,
    val valueIncentive: Long = 2,
    val baseIncentive: Long = 3,
    val dividingThreshold: Long = 100000,
    @Serializable(with = HashSerializer::class)
    val formula: Hash = classDigest<DefaultDiff>(SHA3512Hasher)
) : HashSerializable, ServiceClass {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    private fun getTimeDelta(
        dt: PhysicalData, dt2: PhysicalData
    ): BigDecimal {
        val stamp1 = BigDecimal(dt.millis)
        val stamp2 = BigDecimal(dt2.millis)
        return stamp1
            .subtract(stamp2)
            .divide(stamp1, GlobalLedgerConfiguration.GLOBALCONTEXT)
    }

    fun calculatePayout(
        dt: PhysicalData, dt2: PhysicalData,
        formula: DataFormula
    ): Payout = formula.calculateDiff(
        baseIncentive,
        timeIncentive,
        getTimeDelta(dt, dt2),
        valueIncentive,
        dt.calculateDiff(dt2.data),
        dt.dataConstant,
        dividingThreshold,
        GlobalLedgerConfiguration.GLOBALCONTEXT
    )

    fun calculatePayout(
        dt: PhysicalData, formula: DataFormula
    ): Payout = formula.calculateDiff(
        baseIncentive,
        timeIncentive,
        BigDecimal.ONE,
        valueIncentive,
        BigDecimal.ONE,
        dt.data.dataConstant,
        dividingThreshold,
        GlobalLedgerConfiguration.GLOBALCONTEXT
    )
}
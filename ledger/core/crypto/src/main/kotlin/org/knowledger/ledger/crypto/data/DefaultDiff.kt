@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.crypto.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.hash.Hash
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.crypto.digest.classDigest
import java.math.BigDecimal
import java.math.MathContext

@Serializable
class DefaultDiff(val hashers: Hashers) : DataFormula {
    override val hash: Hash = classDigest(hashers)

    override fun calculateDiff(
        base: Long, timeBase: Long, deltaTime: BigDecimal, valueBase: Long,
        deltaValue: BigDecimal, constant: Long, threshold: Long, mathContext: MathContext,
    ): Payout {
        val standardDivisor = BigDecimal(threshold * constant)
        val timeFactor = deltaTime
            .multiply(BigDecimal(timeBase))
            .pow(2, mathContext)
            .divide(standardDivisor, mathContext)

        val valueFactor = deltaValue
            .divide(BigDecimal(2), mathContext)
            .multiply(BigDecimal(valueBase))
            .divide(standardDivisor, mathContext)

        val baseFactor = BigDecimal(base).divide(standardDivisor, mathContext)
        return Payout(timeFactor.add(valueFactor).add(baseFactor))
    }

}
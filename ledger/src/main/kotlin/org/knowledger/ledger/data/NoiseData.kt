@file:UseSerializers(BigDecimalSerializer::class)
package org.knowledger.ledger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.core.serial.BigDecimalSerializer
import java.io.InvalidClassException
import java.math.BigDecimal


/**
 * Ambient Noise Level measured in dB.
 *
 * Noise level represents either an
 * RMS sampled from a PCM signal in the interval [-1, 1],
 * or a dB relative to the standard base (db SPL).
 *
 * Peak or Base should be either the peak of the PCM signal,
 * or the standard dB base (2*10^5 Pa for dB SPL)
 *
 * Thus care should be taken to understand which unit to use,
 * as specified in [NoiseUnit]
 **/
@Serializable
@SerialName("NoiseData")
data class NoiseData(
    val noiseLevel: BigDecimal,
    val peakOrBase: BigDecimal,
    val unit: NoiseUnit
) : LedgerData {
    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is NoiseData -> calculateDiffNoise(previous)
            else -> throw InvalidClassException(
                """SelfInterval supplied is:
                    |   ${previous.javaClass.name},
                    |   not ${this::class.java.name}
                """.trimMargin()
            )
        }


    private fun calculateDiffNoise(
        previous: NoiseData
    ): BigDecimal {
        val newN =
            noiseLevel
                .add(peakOrBase)
                .abs()
        val oldN =
            previous
                .noiseLevel
                .add(previous.peakOrBase)
                .abs()
        return newN
            .subtract(oldN)
            .divide(
                oldN,
                GLOBALCONTEXT
            )
    }

}

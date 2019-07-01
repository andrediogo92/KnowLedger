package pt.um.masb.ledger.data

import com.squareup.moshi.JsonClass
import pt.um.masb.common.config.LedgerConfiguration
import pt.um.masb.common.data.LedgerData
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
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
 * as specified in [NUnit]
 **/
@JsonClass(generateAdapter = true)
data class NoiseData(
    val noiseLevel: BigDecimal,
    val peakOrBase: BigDecimal,
    val unit: NUnit
) : LedgerData {
    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                noiseLevel.unscaledValue().toByteArray(),
                peakOrBase.unscaledValue().toByteArray(),
                unit.ordinal.bytes()
            )
        )

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
                LedgerConfiguration.GLOBALCONTEXT
            )
    }

}

package org.knowledger.ledger.data

import com.squareup.moshi.JsonClass
import org.knowledger.common.config.LedgerConfiguration
import org.knowledger.common.data.LedgerData
import org.knowledger.common.data.SelfInterval
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.bytes
import org.knowledger.common.misc.flattenBytes
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
                noiseLevel.bytes(),
                peakOrBase.bytes(),
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

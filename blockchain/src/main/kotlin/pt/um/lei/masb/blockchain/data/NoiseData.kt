package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.Coinbase
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
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
data class NoiseData(
    val noiseLevel: BigDecimal,
    val peakOrBase: BigDecimal,
    val unit: NUnit
) : BlockChainData {
    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            $noiseLevel
            $peakOrBase
            ${unit.name}
            ${unit.ordinal}
            """.trimIndent()
        )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Noise")
            .let {
                it.setProperty(
                    "noiseLevel",
                    noiseLevel
                )
                it.setProperty(
                    "peakOrBase",
                    peakOrBase
                )
                it.setProperty(
                    "unit", when (unit) {
                        NUnit.DBSPL -> 0x00.toByte()
                        NUnit.RMS -> 0x01.toByte()
                    }
                )
                it
            }


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is NoiseData -> calculateDiffNoise(previous)
            else ->
                throw InvalidClassException(
                    "SelfInterval supplied is not ${
                    this::class.simpleName
                    }"
                )
        }


    private fun calculateDiffNoise(
        previous: NoiseData
    ): BigDecimal {
        val newN = noiseLevel.add(peakOrBase)
            .abs()
        val oldN = previous.noiseLevel
            .add(previous.peakOrBase)
            .abs()
        return newN.subtract(oldN)
            .divide(
                oldN,
                Coinbase.MATH_CONTEXT
            )
    }

    override fun toString(): String =
        "NoiseData(noiseLevel = $noiseLevel, peakOrBase = $peakOrBase, unit = $unit)"

}

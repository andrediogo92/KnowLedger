package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.lei.masb.blockchain.ledger.Coinbase
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.bytes
import pt.um.lei.masb.blockchain.utils.flattenBytes
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
) : BlockChainData {
    override fun digest(c: Crypter): Hash =
        c.applyHash(
            flattenBytes(
                noiseLevel.unscaledValue().toByteArray(),
                peakOrBase.unscaledValue().toByteArray(),
                unit.ordinal.bytes()
            )
        )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Noise")
            .apply {
                setProperty("noiseLevel", noiseLevel)
                setProperty("peakOrBase", peakOrBase)
                setProperty(
                    "unit", when (unit) {
                        NUnit.DBSPL -> NUnit.DBSPL.ordinal.toByte()
                        NUnit.RMS -> NUnit.RMS.ordinal.toByte()
                    }
                )
            }


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is NoiseData -> calculateDiffNoise(previous)
            else -> throw InvalidClassException(
                "SelfInterval supplied is not ${this::class.java.name}"
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

}

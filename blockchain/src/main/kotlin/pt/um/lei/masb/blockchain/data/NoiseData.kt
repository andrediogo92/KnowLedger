package pt.um.lei.masb.blockchain.data

import pt.um.lei.masb.blockchain.Coinbase
import java.math.BigDecimal


/**
 * Ambient Noise Level measured in dB.
 * <pw>
 *
 * Noise level represents either an
 * RMS sampled from a PCM signal in the interval [-1, 1],
 * or a dB relative to the standard base (db SPL).
 *
 * <pw>
 * Peak or Base should be either the peak of the PCM signal,
 * or the standard dB base (2*10^5 Pa for dB SPL)
 *
 * <pw>
 * Thus care should be taken to understand which unit to use,
 * as specified in {@link NUnit}
 **/
class NoiseData(
        val noiseLevel: BigDecimal,
        val peakOrBase: BigDecimal,
        val unit: NUnit
) : BlockChainData<NoiseData> {

    override fun calculateDiff(previous: NoiseData): BigDecimal {
        val newN = noiseLevel.add(peakOrBase)
            .abs()
        val oldN = previous.noiseLevel.add(previous.peakOrBase)
            .abs()
        return newN.subtract(oldN)
            .divide(oldN, Coinbase.MATH_CONTEXT)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NoiseData) return false

        if (noiseLevel != other.noiseLevel) return false
        if (peakOrBase != other.peakOrBase) return false
        if (unit != other.unit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = noiseLevel.hashCode()
        result = 31 * result + peakOrBase.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }

    override fun toString(): String {
        return "NoiseData(noiseLevel=$noiseLevel, peakOrBase=$peakOrBase, unit=$unit)"
    }

}

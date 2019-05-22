package pt.um.masb.ledger.data

import com.squareup.moshi.JsonClass
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.bytes
import pt.um.masb.ledger.storage.Coinbase
import java.io.InvalidClassException
import java.math.BigDecimal

/**
 * Luminosity data might be output by an ambient light
 * sensor, using lux units ([LUnit.LUX]) or a lighting unit,
 * outputting a specific amount of lumens ([LUnit.LUMENS]),
 * according to [unit].
 */
@JsonClass(generateAdapter = true)
data class LuminosityData(
    val lum: BigDecimal,
    val unit: LUnit
) : BlockChainData {
    override fun digest(c: Hasher): Hash =
        c.applyHash(
            lum.unscaledValue().toByteArray() + unit.ordinal.bytes()
        )


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is LuminosityData -> calculateDiffLum(previous)
            else -> throw InvalidClassException(
                """SelfInterval supplied is:
                    |   ${previous.javaClass.name},
                    |   not ${this::class.java.name}
                """.trimMargin()
            )
        }

    private fun calculateDiffLum(
        previous: LuminosityData
    ): BigDecimal =
        lum.subtract(previous.lum)
            .divide(
                previous.lum,
                Coinbase.MATH_CONTEXT
            )


    override fun toString(): String {
        return "LuminosityData(lum = $lum, unit = $unit)"
    }
}

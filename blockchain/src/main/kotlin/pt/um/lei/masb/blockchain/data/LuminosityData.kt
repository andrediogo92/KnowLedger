package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.Coinbase
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.io.InvalidClassException
import java.math.BigDecimal

/**
 * Luminosity data might be output by an ambient light
 * sensor, using lux units ([LUnit.LUX]) or a lighting unit,
 * outputting a specific amount of lumens ([LUnit.LUMENS]),
 * according to [unit].
 */
data class LuminosityData(
    val lum: BigDecimal,
    val unit: LUnit
) : BlockChainData {
    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            $lum
            ${unit.name}
            ${unit.ordinal}
            """.trimIndent()
        )


    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Luminosity")
            .let {
                it.setProperty("lum", lum)
                it.setProperty(
                    "unit", when (unit) {
                        LUnit.LUMENS -> 0x00.toByte()
                        LUnit.LUX -> 0x01.toByte()
                    }
                )
                it
            }


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is LuminosityData -> calculateDiffLum(previous)
            else ->
                throw InvalidClassException(
                    "SelfInterval supplied is not ${
                    this::class.qualifiedName
                    }"
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

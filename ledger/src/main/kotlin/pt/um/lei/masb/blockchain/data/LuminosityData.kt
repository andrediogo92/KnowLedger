package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.lei.masb.blockchain.ledger.Coinbase
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.bytes
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
    override fun digest(c: Crypter): Hash =
        c.applyHash(
            lum.unscaledValue().toByteArray() + unit.ordinal.bytes()
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
                        LUnit.LUMENS -> LUnit.LUMENS.ordinal
                        LUnit.LUX -> LUnit.LUX.ordinal
                    }
                )
                it
            }


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is LuminosityData -> calculateDiffLum(previous)
            else -> throw InvalidClassException(
                "SelfInterval supplied is not ${this::class.java.name}"
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

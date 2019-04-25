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
 * Temperature data specifies a decimal temperature value
 * and a Temperature unit ([TUnit.CELSIUS],
 * [TUnit.FAHRENHEIT], [TUnit.RANKINE] and [TUnit.KELVIN])
 * with idempotent methods to convert between them as needed.
 */
@JsonClass(generateAdapter = true)
data class TemperatureData(
    val temperature: BigDecimal,
    val unit: TUnit
) : BlockChainData {
    override fun digest(c: Crypter): Hash =
        c.applyHash(
            flattenBytes(
                arrayOf(
                    temperature.unscaledValue().toByteArray(),
                    unit.ordinal.bytes()
                )
            )
        )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Temperature")
            .apply {
                setProperty(
                    "temperature",
                    temperature
                )
                setProperty(
                    "unit",
                    when (unit) {
                        TUnit.CELSIUS -> TUnit.CELSIUS.ordinal
                        TUnit.FAHRENHEIT -> TUnit.FAHRENHEIT.ordinal
                        TUnit.KELVIN -> TUnit.KELVIN.ordinal
                        TUnit.RANKINE -> TUnit.RANKINE.ordinal
                    }
                )
            }

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is TemperatureData -> calculateDiffTemp(previous)
            else -> throw InvalidClassException(
                "SelfInterval supplied is not ${this::class.java.name}"
            )
        }


    private fun calculateDiffTemp(
        previous: TemperatureData
    ): BigDecimal {
        val oldT = previous.unit.convertTo(
            previous.temperature,
            TUnit.CELSIUS
        )
        return unit.convertTo(temperature, TUnit.CELSIUS)
            .subtract(oldT)
            .divide(oldT, Coinbase.MATH_CONTEXT)
    }

}

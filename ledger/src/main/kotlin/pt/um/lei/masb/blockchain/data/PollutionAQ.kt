package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.math.BigDecimal

class PollutionAQ(
    var lastUpdated: String,
    unit: String,
    var parameter: PollutionType,
    value: Double,
    var sourceName: String,
    city: String = "",
    citySeqNum: Int = 1
) : AbstractPollution(
    unit,
    city,
    citySeqNum
) {
    constructor(
        lastUpdated: String,
        unit: String,
        parameter: String,
        value: Double,
        sourceName: String,
        city: String = "",
        citySeqNum: Int = 1
    ) : this(
        lastUpdated,
        unit,
        when (parameter) {
            "pm25" -> PollutionType.PM25
            "pm10" -> PollutionType.PM10
            "co" -> PollutionType.CO
            "bc" -> PollutionType.BC
            "so2" -> PollutionType.SO2
            "no2" -> PollutionType.NO2
            "o3" -> PollutionType.O3
            else -> {
                PollutionType.NA
            }
        },
        value,
        sourceName,
        city,
        citySeqNum
    )


    override fun calculateDiff(previous: SelfInterval): BigDecimal {
        TODO("not implemented")
    }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            $lastUpdated
            $unit
            $parameter
            $valueInternal
            $sourceName
            $city
            $citySeqNum
        """.trimIndent()
        )

    override fun store(session: NewInstanceSession): OElement =
        session.newInstance("PollutionAQ").let {
            it.setProperty("lastUpdated", lastUpdated)
            it.setProperty("unit", unit)
            val byte = when (parameter) {
                PollutionType.PM25 -> 0x00
                PollutionType.PM10 -> 0x01
                PollutionType.SO2 -> 0x02
                PollutionType.NO2 -> 0x03
                PollutionType.O3 -> 0x04
                PollutionType.CO -> 0x05
                PollutionType.BC -> 0x06
                PollutionType.NA -> 0x07
                else -> 0xFF
            }
            //Byte encode the enum.
            it.setProperty("parameter", byte)
            it.setProperty("valueInternal", valueInternal)
            it.setProperty("sourceName", sourceName)
            it.setProperty("city", city)
            it.setProperty("citySeqNum", citySeqNum)
            it
        }

    private var valueInternal = value

    var value: Double
        get() = if (valueInternal.isNaN())
            valueInternal else
            -99.0
        set(v) {
            valueInternal = v
        }




    override fun toString(): String =
        """
        |PollutionAQ {
        |                   Pollution Measurement: $parameter - ${parameter.name}
        |                   Last Update: $lastUpdated
        |                   Value: $value
        |                   Data Source: $sourceName
        |               }
        """.trimMargin()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PollutionAQ) return false

        if (lastUpdated != other.lastUpdated) return false
        if (parameter != other.parameter) return false
        if (sourceName != other.sourceName) return false
        if (valueInternal != other.valueInternal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lastUpdated.hashCode()
        result = 31 * result + parameter.hashCode()
        result = 31 * result + sourceName.hashCode()
        result = 31 * result + valueInternal.hashCode()
        return result
    }

}

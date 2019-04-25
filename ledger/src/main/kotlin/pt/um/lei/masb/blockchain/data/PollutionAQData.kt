package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.bytes
import pt.um.lei.masb.blockchain.utils.flattenBytes
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
class PollutionAQData(
    var lastUpdated: String,
    unit: String,
    var parameter: PollutionType,
    var value: Double,
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
            flattenBytes(
                lastUpdated.toByteArray(),
                unit.toByteArray(),
                parameter.ordinal.bytes(),
                value.bytes(),
                sourceName.toByteArray(),
                city.toByteArray(),
                citySeqNum.bytes()
            )
        )

    override fun store(session: NewInstanceSession): OElement =
        session.newInstance("PollutionAQData").apply {
            setProperty("lastUpdated", lastUpdated)
            setProperty("unit", unit)
            val byte = when (parameter) {
                PollutionType.PM25 -> PollutionType.PM25.ordinal
                PollutionType.PM10 -> PollutionType.PM10.ordinal
                PollutionType.SO2 -> PollutionType.SO2.ordinal
                PollutionType.NO2 -> PollutionType.NO2.ordinal
                PollutionType.O3 -> PollutionType.O3.ordinal
                PollutionType.CO -> PollutionType.CO.ordinal
                PollutionType.BC -> PollutionType.BC.ordinal
                PollutionType.NA -> PollutionType.NA.ordinal
                else -> Int.MAX_VALUE
            }
            //Byte encode the enum.
            setProperty("parameter", byte)
            setProperty("valueInternal", value)
            setProperty("sourceName", sourceName)
            setProperty("city", city)
            setProperty("citySeqNum", citySeqNum)
        }




    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PollutionAQData) return false

        if (lastUpdated != other.lastUpdated) return false
        if (parameter != other.parameter) return false
        if (sourceName != other.sourceName) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lastUpdated.hashCode()
        result = 31 * result + parameter.hashCode()
        result = 31 * result + sourceName.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String {
        return "PollutionAQData(lastUpdated='$lastUpdated', parameter=$parameter, sourceName='$sourceName', valueInternal=$value)"
    }

}

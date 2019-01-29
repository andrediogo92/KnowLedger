package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.math.BigDecimal
import java.util.*

class PollutionAQ(
    lat: Double,
    lon: Double,
    var lastUpdated: String,
    unit: String,
    var parameter: PollutionType,
    value: Double,
    var sourceName: String,
    date: Long = System.currentTimeMillis(),
    city: String = "",
    citySeqNum: Int = 1
) : AbstractPollution(
    lat,
    lon,
    date,
    unit,
    city,
    citySeqNum
) {
    constructor(
        lat: Double,
        lon: Double,
        lastUpdated: String,
        unit: String,
        parameter: String,
        value: Double,
        sourceName: String,
        date: Long = System.currentTimeMillis(),
        city: String = "",
        citySeqNum: Int = 1
    ) : this(
        lat,
        lon,
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
        date,
        city,
        citySeqNum
    )


    override fun calculateDiff(previous: SelfInterval): BigDecimal {
        TODO("not implemented")
    }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            $lat
            $lon
            $lastUpdated
            $unit
            $parameter
            $valueInternal
            $sourceName
            $date
            $city
            $citySeqNum
        """.trimIndent()
        )

    override fun store(session: NewInstanceSession): OElement =
        session.newInstance("PollutionAQ").let {
            it.setProperty("lat", lat)
            it.setProperty("lon", lon)
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
            }
            //Byte encode the enum.
            it.setProperty("parameter", byte)
            it.setProperty("valueInternal", valueInternal)
            it.setProperty("sourceName", sourceName)
            it.setProperty("date", date)
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


    enum class PollutionType {
        PM25 {
            override fun toString(): String {
                return "Particulate Matter 2.5"
            }
        },
        PM10 {
            override fun toString(): String {
                return "Particulate Matter 10"
            }
        },
        SO2 {
            override fun toString(): String {
                return "Sulfur Dioxide"
            }
        },
        NO2 {
            override fun toString(): String {
                return "Nitrogen Dioxide"
            }
        },
        O3 {
            override fun toString(): String {
                return "Ozone"
            }
        },
        CO {
            override fun toString(): String {
                return "Carbon Monoxide"
            }
        },
        BC {
            override fun toString(): String {
                return "Black Carbon"
            }
        },
        NA {
            override fun toString(): String {
                return "Non Available"
            }
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("******** Pollution Measurement - ").append(this.parameter.toString()).append(" - ")
            .append(this.parameter.name).append(" ********").append(System.getProperty("line.separator"))
        sb.append("Latitude: ").append(this.lat).append(System.getProperty("line.separator"))
        sb.append("Longitude: ").append(this.lon).append(System.getProperty("line.separator"))
        sb.append("Date: ").append(Date(this.date)).append(System.getProperty("line.separator"))
        sb.append("Last Update Date: ").append(this.lastUpdated).append(System.getProperty("line.separator"))
        sb.append("Parameter: ").append(this.parameter.toString()).append(System.getProperty("line.separator"))
        sb.append("Value: ").append(this.value).append(" ").append(this.unit)
            .append(System.getProperty("line.separator"))
        sb.append("Data Source: ").append(this.sourceName).append(System.getProperty("line.separator"))
        return sb.toString()
    }

}

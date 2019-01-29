package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.math.BigDecimal


class PollutionOWM(
    lat: Double,
    lon: Double,
    date: Long,
    unit: String,
    var parameter: PollutionType,
    value: Double,
    var data: List<List<Double>>,
    city: String = "",
    citySeqNum: Int = 1
) : AbstractPollution(
    lat,
    lon,
    date * 1000,
    unit,
    city,
    citySeqNum
) {
    constructor(
        lat: Double,
        lon: Double,
        date: Long,
        unit: String,
        parameter: String,
        value: Double,
        data: List<List<Double>>,
        city: String = "",
        citySeqNum: Int = 1
    ) : this(
        lat,
        lon,
        date,
        unit,
        when (parameter) {
            "O3" -> PollutionType.O3
            "UV" -> PollutionType.UV
            "CO" -> PollutionType.CO
            "SO2" -> PollutionType.SO2
            "NO2" -> PollutionType.NO2
            else -> PollutionType.NA
        },
        when (parameter) {
            "O3", "UV" -> value
            else -> -99.0
        },
        when (parameter) {
            "CO", "SO2", "NO2" -> clone(data)
            else -> emptyList()
        },
        city,
        citySeqNum
    )

    override fun calculateDiff(previous: SelfInterval): BigDecimal {
        TODO("calculateDiff not implemented")
    }

    override fun digest(c: Crypter): Hash =
        c.applyHash("""
            $lat
            $lon
            $date
            $unit
            $parameter
            $valueInternal
            $city
            $citySeqNum
            ${
        data.joinToString { ld ->
            ld.joinToString {
                it.toString()
            }
        }
        }
        """.trimIndent())

    override fun store(session: NewInstanceSession): OElement =
        session.newInstance("PollutionOWM").let {
            it.setProperty("lat", lat)
            it.setProperty("lon", lon)
            it.setProperty("date", date)
            val byte = when (parameter) {
                PollutionType.O3 -> 0x00
                PollutionType.UV -> 0x01
                PollutionType.CO -> 0x02
                PollutionType.SO2 -> 0x03
                PollutionType.NO2 -> 0x04
                PollutionType.NA -> 0x05
            }
            it.setProperty("parameter", byte)
            it.setProperty("valueInternal", valueInternal)
            it.setProperty("unit", unit)
            it.setProperty("city", city)
            it.setProperty("data", emptyList<List<Double>>())
            it.setProperty("citySeqNum", citySeqNum)
            it
        }

    var valueInternal = value
    var value: Double
        get() = if (!valueInternal.isNaN())
            valueInternal
        else
            -99.0
        set(v) {
            valueInternal = v
        }


    //mean of Value/Precision
    //Value
    //Precision
    val meanValuePrecision: DoubleArray
        get() {
            val mean = doubleArrayOf(0.0, 0.0)
            var validElements = 0
            if (value == -99.0) {
                for (values in data) {
                    if (!values[0].isNaN() && !values[1].isNaN()) {
                        mean[0] += values[0]
                        mean[1] += values[1]
                        validElements++
                    }
                }
                mean[0] = mean[0] / validElements
                mean[1] = mean[1] / validElements
            }
            return mean
        }

    enum class PollutionType {
        O3 {
            override fun toString(): String {
                return "Ozone"
            }
        },
        UV {
            override fun toString(): String {
                return "Ultraviolet"
            }
        },
        CO {
            override fun toString(): String {
                return "Carbon Monoxide"
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
        NA {
            override fun toString(): String {
                return "Non Available"
            }
        }
    }


    override fun toString(): String =
        """
        |PollutionOWM {
        |                   Pollution Measurement: $parameter - ${parameter.name}
        |                   Latitude: $lat
        |                   Longitude: $lon
        |                   Date: $date
        |                   ${
        when (parameter) {
            PollutionType.O3, PollutionType.UV -> "Value: $value $unit"
            PollutionType.NA -> "Value: NA"
            else ->
                """Data {
                |${data.joinToString(
                    ","
                ) {
                    """
                    |Value: ${data[0]} $unit
                    |Precision: ${data[1]}
                    """.trimMargin()
                }}"""
        }
        }
        |               }
        """.trimMargin()


    companion object {
        private fun clone(data: List<List<Double>>): List<List<Double>> {
            val cloned = mutableListOf<List<Double>>()

            for (d in data) {
                val inner = mutableListOf<Double>()
                for (i in d.indices)
                    inner.add(d[i])
                cloned.add(inner)
            }

            return cloned
        }

    }
}

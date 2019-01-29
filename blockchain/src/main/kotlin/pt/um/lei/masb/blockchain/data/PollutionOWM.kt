package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.math.BigDecimal
import java.util.*


class PollutionOWM(
    lat: Double,
    lon: Double,
    date: Long,
    unit: String,
    parameter: String,
    value: Double,
    data: List<List<Double>>
) : AbstractPollution(
    lat,
    lon,
    date * 1000,
    unit
) {
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
            it.setProperty("parameter", parameter)
            it.setProperty("valueInternal", valueInternal)
            it.setProperty("data", data)
            it.setProperty("unit", unit)
            it
        }

    var parameter: PollutionType
    var valueInternal = value
    var value: Double
        get() = if (!valueInternal.isNaN())
            valueInternal
        else
            -99.0
        set(v) {
            valueInternal = v
        }

    //Lists of Value and Precision to be used
    var data: List<List<Double>>

    //mean of Value/Precision
    //Value
    //Precision
    val meanValuePrecision: DoubleArray
        get() {
            val mean = doubleArrayOf(0.0, 0.0)
            var validElements = 0
            if (value == -99.0) {
                for (values in data) {
                    if (!java.lang.Double.isNaN(values[0]) && !java.lang.Double.isNaN(values[1])) {
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

    init {
        when (parameter) {
            "O3" -> {
                this.parameter = PollutionType.O3
                this.value = value
                this.data = emptyList()
            }
            "UV" -> {
                this.parameter = PollutionType.UV
                this.value = value
                this.data = emptyList()
            }
            "CO" -> {
                this.parameter = PollutionType.CO
                this.value = -99.0
                this.data = clone(data)
            }
            "SO2" -> {
                this.parameter = PollutionType.SO2
                this.value = -99.0
                this.data = clone(data)
            }
            "NO2" -> {
                this.parameter = PollutionType.NO2
                this.value = -99.0
                this.data = clone(data)
            }
            else -> {
                this.parameter = PollutionType.NA
                this.value = -99.0
                this.data = emptyList()
            }
        }
    }

    private fun clone(data: List<List<Double>>): List<List<Double>> {
        val cloned = ArrayList<List<Double>>()

        for (d in data) {
            val inner = ArrayList<Double>()
            for (i in d.indices)
                inner.add(d[i])
            cloned.add(inner)
        }

        return cloned
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("********  Pollution Measurement - ").append(this.parameter.toString()).append(" - ")
            .append(this.parameter.name).append(" ********").append(System.getProperty("line.separator"))
        sb.append("Latitude: ").append(this.lat).append(System.getProperty("line.separator"))
        sb.append("Longitude: ").append(this.lon).append(System.getProperty("line.separator"))
        sb.append("Date: ").append(Date(this.date)).append(System.getProperty("line.separator"))

        if (this.parameter === PollutionType.NA) {
            return sb.toString()
        } else if (this.parameter === PollutionType.O3 || this.parameter === PollutionType.UV) {
            sb.append("Value: ").append(this.value).append(" ").append(this.unit)
                .append(System.getProperty("line.separator"))
        } else {
            for (d in this.data) {
                sb.append("Value: ").append(d[0]).append(" ").append(this.unit)
                    .append(System.getProperty("line.separator"))
                sb.append("Precision: ").append(d[1]).append(System.getProperty("line.separator"))
                sb.append("---").append(System.getProperty("line.separator"))
            }
        }

        return sb.toString()
    }

}

package pt.um.masb.ledger.data

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.masb.common.Hash
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import java.math.BigDecimal


@JsonClass(generateAdapter = true)
class PollutionOWMData(
    unit: String,
    var parameter: PollutionType,
    value: Double,
    var data: List<List<Double>>,
    city: String = "",
    citySeqNum: Int = 1
) : pt.um.masb.ledger.data.AbstractPollution(
    unit,
    city,
    citySeqNum
) {
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


    constructor(
        unit: String,
        parameter: String,
        value: Double,
        data: List<List<Double>>,
        city: String = "",
        citySeqNum: Int = 1
    ) : this(
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
            "CO", "SO2", "NO2" -> data.asSequence().map {
                it.asSequence().toList()
            }.toList()
            else -> emptyList()
        },
        city,
        citySeqNum
    )

    override fun calculateDiff(previous: SelfInterval): BigDecimal {
        TODO("calculateDiff not implemented")
    }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            flattenBytes(
                data.asIterable().flatMap { fl ->
                    fl.asIterable().map {
                        it.bytes()
                    }
                },
                unit.toByteArray(),
                value.bytes(),
                city.toByteArray(),
                citySeqNum.bytes(),
                parameter.ordinal.bytes()
            )
        )

    override fun store(session: NewInstanceSession): OElement =
        session.newInstance("PollutionOWMData").apply {
            val parameter = when (parameter) {
                PollutionType.O3 -> PollutionType.O3.ordinal
                PollutionType.UV -> PollutionType.UV.ordinal
                PollutionType.CO -> PollutionType.CO.ordinal
                PollutionType.SO2 -> PollutionType.SO2.ordinal
                PollutionType.NO2 -> PollutionType.NO2.ordinal
                PollutionType.NA -> PollutionType.NA.ordinal
                else -> Int.MAX_VALUE
            }
            setProperty("parameter", parameter)
            setProperty("valueInternal", valueInternal)
            setProperty("unit", unit)
            setProperty("city", city)
            setProperty("data", emptyList<List<Double>>())
            setProperty("citySeqNum", citySeqNum)
        }

    override fun toString(): String {
        return "PollutionOWMData(parameter=$parameter, data=$data, valueInternal=$valueInternal)"
    }


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

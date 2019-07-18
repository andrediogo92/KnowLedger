package org.knowledger.ledger.data

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.bytes
import org.knowledger.ledger.core.misc.encodeStringToUTF8
import org.knowledger.ledger.core.misc.flattenBytes
import java.io.InvalidClassException
import java.math.BigDecimal


@JsonClass(generateAdapter = true)
class PollutionOWMData(
    unit: String,
    var parameter: PollutionType,
    val value: Double,
    var data: List<List<Double>>,
    city: String = "",
    citySeqNum: Int = 1
) : AbstractPollution(
    unit,
    city,
    citySeqNum
) {
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
        return if (previous is PollutionOWMData) {
            calculateDiffPollution(previous)
        } else {
            throw InvalidClassException(
                """SelfInterval supplied is:
                    |   ${previous.javaClass.name},
                    |   not ${this::class.java.name}
                """.trimMargin()
            )
        }
    }

    private fun calculateDiffPollution(
        previous: PollutionOWMData
    ): BigDecimal {
        TODO()
    }

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                data.asIterable().flatMap { fl ->
                    fl.asIterable().map {
                        it.bytes()
                    }
                },
                unit.encodeStringToUTF8(),
                value.bytes(),
                city.encodeStringToUTF8(),
                citySeqNum.bytes(),
                parameter.ordinal.bytes()
            )
        )


    override fun toString(): String {
        return "PollutionOWMData(parameter=$parameter, value=$value, value=$data)"
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

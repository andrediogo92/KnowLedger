package org.knowledger.ledger.data

import com.squareup.moshi.JsonClass
import org.knowledger.common.data.SelfInterval
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.bytes
import org.knowledger.common.misc.encodeStringToUTF8
import org.knowledger.common.misc.flattenBytes
import java.io.InvalidClassException
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


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal {
        return if (previous is PollutionAQData) {
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
        previous: PollutionAQData
    ): BigDecimal {
        TODO()
    }

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                lastUpdated.encodeStringToUTF8(),
                unit.encodeStringToUTF8(),
                parameter.ordinal.bytes(),
                value.bytes(),
                sourceName.encodeStringToUTF8(),
                city.encodeStringToUTF8(),
                citySeqNum.bytes()
            )
        )

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

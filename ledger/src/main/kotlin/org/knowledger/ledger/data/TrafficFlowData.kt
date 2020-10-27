package org.knowledger.ledger.data


import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.SelfInterval
import java.io.InvalidClassException
import java.math.BigDecimal

/**
 *
 * Traffic Flow (https://developer.tomtom.com/online-traffic/online-traffic-documentation/flow-segment-value):
 * https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json?key=<API_KEY>&point=41.503122,-8.480000
 *
 **/
@Serializable
@SerialName("TrafficFlowData")
data class TrafficFlowData(
    val functionalRoadClass: String,    //Indicates the road type
    val currentSpeed: Int,  //Current speed
    val freeFlowSpeed: Int, //Free flow speed expected under ideal conditions
    val currentTravelTime: Int, //Current travel time in sec based on fused real-time measurements
    val freeFlowTravelTime: Int,    //The travel time in sec which would be expected under ideal free flow conditions
    val confidence: Double,   //Measure of the quality of the provided travel time and speed
    val realtimeRatio: Double,   //The ratio between live and the historical value used to provide the response
    val city: String = "TBD",
    val citySeqNum: Int = 1,
) : LedgerData {
    //Indicates the road type
    val functionalRoadClassDescription: String =
        when (functionalRoadClass) {
            "FRC0" -> "Motorway"
            "FRC1" -> "Major Road"
            "FRC2" -> "Other Major Road"
            "FRC3" -> "Secondary Road"
            "FRC4" -> "Local Connecting Road"
            "FRC5" -> "Local High Importance Road"
            "FRC6" -> "Local Road"
            else -> "Unknown Road"
        }

    override fun clone(): TrafficFlowData = copy()


    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(serializer(), this)


    override fun calculateDiff(previous: SelfInterval): BigDecimal =
        when (previous) {
            is TrafficFlowData -> calculateDiffTraffic(previous)
            else -> throw InvalidClassException(
                """SelfInterval supplied is:
                |   ${previous.javaClass.name},
                |   not ${this::class.java.name}
                """.trimMargin()
            )
        }

    private fun calculateDiffTraffic(previous: TrafficFlowData): BigDecimal {
        return BigDecimal.ONE
    }
}

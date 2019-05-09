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

/**
 *
 * Traffic Flow (https://developer.tomtom.com/online-traffic/online-traffic-documentation/flow-segment-data):
 * https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json?key=<API_KEY>&point=41.503122,-8.480000
 *
 **/
@JsonClass(generateAdapter = true)
class TrafficFlowData(
    var functionalRoadClass: String,    //Indicates the road type
    var currentSpeed: Int,  //Current speed
    var freeFlowSpeed: Int, //Free flow speed expected under ideal conditions
    var currentTravelTime: Int, //Current travel time in sec based on fused real-time measurements
    var freeFlowTravelTime: Int,    //The travel time in sec which would be expected under ideal free flow conditions
    var confidence: Double,   //Measure of the quality of the provided travel time and speed
    var realtimeRatio: Double,   //The ratio between live and the historical data used to provide the response
    city: String = "TBD",
    citySeqNum: Int = 1
) : pt.um.masb.ledger.data.AbstractTrafficIncident(
    city,
    citySeqNum
) {
    override fun calculateDiff(previous: SelfInterval): BigDecimal {
        TODO("calculateDiff not implemented")
    }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            flattenBytes(
                functionalRoadClass.toByteArray(),
                currentSpeed.bytes(),
                freeFlowSpeed.bytes(),
                currentTravelTime.bytes(),
                freeFlowTravelTime.bytes(),
                confidence.bytes(),
                realtimeRatio.bytes(),
                cityName.toByteArray(),
                citySeqNum.bytes()
            )
        )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session.newInstance("TrafficFlowData").apply {
            setProperty("functionalRoadClass", functionalRoadClass)
            setProperty("currentSpeed", currentSpeed)
            setProperty("freeFlowSpeed", freeFlowSpeed)
            setProperty("currentTravelTime", currentTravelTime)
            setProperty("freeFlowTravelTime", freeFlowTravelTime)
            setProperty("confidence", confidence)
            setProperty("realtimeRatio", realtimeRatio)
            setProperty("cityName", cityName)
            setProperty("citySeqNum", citySeqNum)
        }

    //Indicates the road type
    var functionalRoadClassDesc: String

    init {
        when (this.functionalRoadClass) {
            "FRC0" -> this.functionalRoadClassDesc = "Motorway"
            "FRC1" -> this.functionalRoadClassDesc = "Major Road"
            "FRC2" -> this.functionalRoadClassDesc = "Other Major Road"
            "FRC3" -> this.functionalRoadClassDesc = "Secondary Road"
            "FRC4" -> this.functionalRoadClassDesc = "Local Connecting Road"
            "FRC5" -> this.functionalRoadClassDesc = "Local High Importance Road"
            "FRC6" -> this.functionalRoadClassDesc = "Local Road"
            else -> {
                this.functionalRoadClassDesc = "Unknown Road"
            }
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrafficFlowData) return false

        if (functionalRoadClass != other.functionalRoadClass) return false
        if (currentSpeed != other.currentSpeed) return false
        if (freeFlowSpeed != other.freeFlowSpeed) return false
        if (currentTravelTime != other.currentTravelTime) return false
        if (freeFlowTravelTime != other.freeFlowTravelTime) return false
        if (confidence != other.confidence) return false
        if (realtimeRatio != other.realtimeRatio) return false
        if (functionalRoadClassDesc != other.functionalRoadClassDesc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = functionalRoadClass.hashCode()
        result = 31 * result + currentSpeed
        result = 31 * result + freeFlowSpeed
        result = 31 * result + currentTravelTime
        result = 31 * result + freeFlowTravelTime
        result = 31 * result + confidence.hashCode()
        result = 31 * result + realtimeRatio.hashCode()
        result = 31 * result + functionalRoadClassDesc.hashCode()
        return result
    }

    override fun toString(): String {
        return "TrafficFlowData(functionalRoadClass='$functionalRoadClass', currentSpeed=$currentSpeed, freeFlowSpeed=$freeFlowSpeed, currentTravelTime=$currentTravelTime, freeFlowTravelTime=$freeFlowTravelTime, confidence=$confidence, realtimeRatio=$realtimeRatio, functionalRoadClassDesc='$functionalRoadClassDesc')"
    }

    companion object {

        //Functional Road Class Constants
        val MOTORWAY = 0
        val MAJOR_ROAD = 1
        val OTHER_MAJOR_ROAD = 2
        val SECONDARY_ROAD = 3
        val LOCAL_CONNECTING_ROAD = 4
        val LOCAL_HIGH_IMP_ROAD = 5
        val LOCAL_ROAD = 6
    }

}

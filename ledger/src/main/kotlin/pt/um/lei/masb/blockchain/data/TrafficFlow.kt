package pt.um.lei.masb.blockchain.data


import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.bytes
import pt.um.lei.masb.blockchain.utils.flattenBytes
import java.math.BigDecimal

/**
 *
 * Traffic Flow (https://developer.tomtom.com/online-traffic/online-traffic-documentation/flow-segment-data):
 * https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json?key=<API_KEY>&point=41.503122,-8.480000
 *
 **/
class TrafficFlow(
    var functionalRoadClass: String,    //Indicates the road type
    var currentSpeed: Int,  //Current speed
    var freeFlowSpeed: Int, //Free flow speed expected under ideal conditions
    var currentTravelTime: Int, //Current travel time in sec based on fused real-time measurements
    var freeFlowTravelTime: Int,    //The travel time in sec which would be expected under ideal free flow conditions
    confidence: Double,   //Measure of the quality of the provided travel time and speed
    realtimeRatio: Double,   //The ratio between live and the historical data used to provide the response
    city: String = "TBD",
    citySeqNum: Int = 1
) : AbstractTrafficIncident(
    city,
    citySeqNum
) {
    private var confidenceInternal: Double = confidence   //Measure of the quality of the provided travel time and speed
    private var realtimeRatioInternal: Double =
        realtimeRatio   //The ratio between live and the historical data used to provide the response

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
                confidenceInternal.bytes(),
                realtimeRatioInternal.bytes(),
                cityName.toByteArray(),
                citySeqNum.bytes()
            )
        )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session.newInstance("TrafficFlow").apply {
            setProperty("functionalRoadClass", functionalRoadClass)
            setProperty("currentSpeed", currentSpeed)
            setProperty("freeFlowSpeed", freeFlowSpeed)
            setProperty("currentTravelTime", currentTravelTime)
            setProperty("freeFlowTravelTime", freeFlowTravelTime)
            setProperty("confidenceInternal", confidenceInternal)
            setProperty("realtimeRatioInternal", realtimeRatioInternal)
            setProperty("cityName", cityName)
            setProperty("citySeqNum", citySeqNum)
        }

    var functionalRoadClassDesc: String        //Indicates the road type

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

    var confidence
        get() =
            if (!confidenceInternal.isNaN())
                confidenceInternal
            else
                -99.0
        set(value) {
            confidenceInternal = value
        }

    var realtimeRatio
        get() =
            if (!realtimeRatioInternal.isNaN())
                realtimeRatioInternal
            else
                -99.0
        set(value) {
            realtimeRatioInternal = value
        }


    override fun toString(): String =
        """
        |Traffic Flow {
        |                   Functional Road Class Id: $functionalRoadClass,
        |                   Functional Road Class: $functionalRoadClassDesc,
        |                   Current Speed: $currentSpeed
        |                   Current Travel Time: $currentTravelTime
        |                   Free Flow Speed: $freeFlowSpeed
        |                   Free Flow Travel Time: $freeFlowTravelTime
        |                   Confidence: $confidence
        |                   Ratio between live and the historical data: $realtimeRatio
        |               }
        """.trimMargin()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrafficFlow) return false

        if (functionalRoadClass != other.functionalRoadClass) return false
        if (currentSpeed != other.currentSpeed) return false
        if (freeFlowSpeed != other.freeFlowSpeed) return false
        if (currentTravelTime != other.currentTravelTime) return false
        if (freeFlowTravelTime != other.freeFlowTravelTime) return false
        if (confidenceInternal != other.confidenceInternal) return false
        if (realtimeRatioInternal != other.realtimeRatioInternal) return false
        if (functionalRoadClassDesc != other.functionalRoadClassDesc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = functionalRoadClass.hashCode()
        result = 31 * result + currentSpeed
        result = 31 * result + freeFlowSpeed
        result = 31 * result + currentTravelTime
        result = 31 * result + freeFlowTravelTime
        result = 31 * result + confidenceInternal.hashCode()
        result = 31 * result + realtimeRatioInternal.hashCode()
        result = 31 * result + functionalRoadClassDesc.hashCode()
        return result
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

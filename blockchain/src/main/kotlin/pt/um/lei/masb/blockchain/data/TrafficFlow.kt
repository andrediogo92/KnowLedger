package pt.um.lei.masb.blockchain.data


import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.math.BigDecimal
import java.util.*

/**
 *
 * Traffic Flow (https://developer.tomtom.com/online-traffic/online-traffic-documentation/flow-segment-data):
 * https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json?key=<API_KEY>&point=41.503122,-8.480000
 *
 **/
class TrafficFlow(
    trafficLat: Double,
    trafficLon: Double,
    date: Long,
    var functionalRoadClass: String,    //Indicates the road type
    var currentSpeed: Int,  //Current speed
    var freeFlowSpeed: Int, //Free flow speed expected under ideal conditions
    var currentTravelTime: Int, //Current travel time in sec based on fused real-time measurements
    var freeFlowTravelTime: Int,    //The travel time in sec which would be expected under ideal free flow conditions
    private var confidenceInternal: Double,   //Measure of the quality of the provided travel time and speed
    private var realtimeRatioInternal: Double   //The ratio between live and the historical data used to provide the response
) : AbstractTrafficIncident(
    trafficLat,
    trafficLon,
    date,
    "TBD"
) {
    override fun calculateDiff(previous: SelfInterval): BigDecimal {
        TODO("calculateDiff not implemented")
    }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            $trafficLat
            $trafficLon
            $date
            $functionalRoadClass
            $currentSpeed
            $freeFlowSpeed
            $currentTravelTime
            $freeFlowTravelTime
            $confidenceInternal
            $realtimeRatioInternal
            $cityName
            $citySeqNum
            """.trimIndent()
        )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session.newInstance("TrafficFlow").let {
            it.setProperty("trafficLat", trafficLat)
            it.setProperty("trafficLon", trafficLon)
            it.setProperty("date", date)
            it.setProperty("functionalRoadClass", functionalRoadClass)
            it.setProperty("currentSpeed", currentSpeed)
            it.setProperty("freeFlowSpeed", freeFlowSpeed)
            it.setProperty("currentTravelTime", currentTravelTime)
            it.setProperty("freeFlowTravelTime", freeFlowTravelTime)
            it.setProperty("confidenceInternal", confidenceInternal)
            it.setProperty("realtimeRatioInternal", realtimeRatioInternal)
            it.setProperty("cityName", cityName)
            it.setProperty("citySeqNum", citySeqNum)
            it
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


    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("******** Traffic Flow ********").append(System.getProperty("line.separator"))
        sb.append("Date: ").append(Date(this.date)).append(System.getProperty("line.separator"))
        sb.append("Incident Latitude: ").append(this.trafficLat).append(System.getProperty("line.separator"))
        sb.append("Incident Longitude: ").append(this.trafficLon).append(System.getProperty("line.separator"))
        sb.append("Functional Road Class Id: ").append(this.functionalRoadClass)
            .append(System.getProperty("line.separator"))
        sb.append("Functional Road Class: ").append(this.functionalRoadClassDesc)
            .append(System.getProperty("line.separator"))
        sb.append("Current Speed: ").append(this.currentSpeed).append(System.getProperty("line.separator"))
        sb.append("Current Travel Time: ").append(this.currentTravelTime).append(System.getProperty("line.separator"))
        sb.append("Free Flow Speed: ").append(this.freeFlowSpeed).append(System.getProperty("line.separator"))
        sb.append("Free Flow Travel Time: ").append(this.freeFlowTravelTime)
            .append(System.getProperty("line.separator"))
        sb.append("Confidence: ").append(this.confidence).append(System.getProperty("line.separator"))
        sb.append("Ratio between live and the historical data: ").append(this.realtimeRatio)
            .append(System.getProperty("line.separator"))
        return sb.toString()
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

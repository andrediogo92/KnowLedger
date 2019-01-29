package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.math.BigDecimal

/**
 *
 * Traffic Incidents (https://developer.tomtom.com/online-traffic/online-traffic-documentation-online-traffic-incidents/traffic-incident-details):
 * https://api.tomtom.com/traffic/services/4/incidentDetails/s3/41.506531,-8.451247,41.574115,-8.371253/11/1526494296871/xml?key=<API_KEY>&projection=EPSG4326&language=en&expandCluster=true
 *
 *
 **/
class TrafficIncident(
    trafficLat: Double,
    trafficLon: Double,
    date: Long,
    var trafficModelId: String, //Current traffic model
    var id: String, //ID of the traffic incident
    var iconLat: Double,    //The point where an icon of the cluster or raw incident should be drawn
    var iconLon: Double,    //The point where an icon of the cluster or raw incident should be drawn
    var incidentCategory: Int,  //The category associated with this incident. Values are numbers in the range 0-13
    var magnitudeOfDelay: Int,  //The magnitude of delay associated with the incident
    var clusterSize: Int,   //The number of incidents in the cluster
    var description: String,    //Description of the incident in the requested language
    var causeOfAccident: String,    //Cause of the incident, where available, in the requested language
    var from: String,   //The name of the intersection or location where the traffic due to the incident starts
    var to: String, //The name of the intersection or location where the traffic due to the incident ends
    var length: Int,    //Length of the incident in meters
    var delayInSeconds: Int,    //Delay caused by the incident in seconds (except in road closures)
    var affectedRoads: String,   //The road number/s affected by the incident. Multiple road numbers will delimited by slashes.
    city: String = "TBD",
    citySeqNum: Int = 1
) : AbstractTrafficIncident(
    trafficLat,
    trafficLon,
    date,
    city,
    citySeqNum
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
            $trafficModelId
            $id
            $iconLat
            $iconLon
            $incidentCategory
            $magnitudeOfDelay
            $clusterSize
            $description
            $causeOfAccident
            $from
            $to
            $length
            $delayInSeconds
            $affectedRoads
            $cityName
            $citySeqNum
            """.trimIndent()
        )

    override fun store(session: NewInstanceSession): OElement =
        session.newInstance("TrafficFlow").let {
            it.setProperty("trafficLat", trafficLat)
            it.setProperty("trafficLon", trafficLon)
            it.setProperty("date", date)
            it.setProperty("trafficModelId", trafficModelId)
            it.setProperty("id", id)
            it.setProperty("iconLat", iconLat)
            it.setProperty("iconLon", iconLon)
            it.setProperty("incidentCategory", incidentCategory)
            it.setProperty("magnitudeOfDelay", magnitudeOfDelay)
            it.setProperty("clusterSize", clusterSize)
            it.setProperty("description", description)
            it.setProperty("causeOfAccident", causeOfAccident)
            it.setProperty("from", from)
            it.setProperty("to", to)
            it.setProperty("length", length)
            it.setProperty("delayInSeconds", delayInSeconds)
            it.setProperty("affectedRoads", affectedRoads)
            it.setProperty("cityName", cityName)
            it.setProperty("citySeqNum", citySeqNum)
            it
        }


    var incidentCategoryDesc: String    //The category description associated with this incident
    var magnitudeOfDelayDesc: String    //The magnitude of delay description associated with the incident

    init {
        when (this.incidentCategory) {
            CLUSTER -> this.incidentCategoryDesc = "Cluster"
            DETOUR -> this.incidentCategoryDesc = "Detour"
            FLOODING -> this.incidentCategoryDesc = "Flooding"
            WIND -> this.incidentCategoryDesc = "Wind"
            ROAD_WORKS -> this.incidentCategoryDesc = "Road Works"
            ROAD_CLOSED -> this.incidentCategoryDesc = "Road Closed"
            LANE_CLOSED -> this.incidentCategoryDesc = "Lane Closed"
            JAM -> this.incidentCategoryDesc = "Jam"
            ICE -> this.incidentCategoryDesc = "Ice"
            RAIN -> this.incidentCategoryDesc = "Rain"
            DANGEROUS_CONDITIONS -> this.incidentCategoryDesc = "Dangerous Conditions"
            FOG -> this.incidentCategoryDesc = "Fog"
            ACCIDENT -> this.incidentCategoryDesc = "Accident"
            UNKNOWN -> this.incidentCategoryDesc = "Unknown Incident"
            else -> {
                this.incidentCategoryDesc = "Unknown Incident"
            }
        }
        when (this.magnitudeOfDelay) {
            UNDEFINED -> this.magnitudeOfDelayDesc = "Undefined"
            MAJOR -> this.magnitudeOfDelayDesc = "Major"
            MODERATE -> this.magnitudeOfDelayDesc = "Moderate"
            MINOR -> this.magnitudeOfDelayDesc = "Minor"
            UNKNOWN_DELAY -> this.magnitudeOfDelayDesc = "Unknown Delay"
            else -> {
                this.magnitudeOfDelayDesc = "Unknown Delay"
            }
        }
    }


    override fun toString(): String =
        """
        |TrafficIncident {
        |                   Id: $id,
        |                   Date: $date,
        |                   Traffic Model Id: $id,
        |                   Incident Latitude: $trafficLat
        |                   Incident Longitude: $trafficLon
        |                   Icon Latitude: $iconLat
        |                   Icon Longitude: $iconLon
        |                   Incident Category Id: $incidentCategory
        |                   Incident Category: $incidentCategoryDesc
        |                   Magnitude of Delay Id: $magnitudeOfDelay
        |                   Magnitude of Delay: $magnitudeOfDelayDesc
        |                   Cluster Size: $clusterSize
        |                   Description: $description
        |                   Cause of Accident: $causeOfAccident
        |                   From: $from
        |                   To: $to
        |                   Length in meters: $length
        |                   Delay in seconds: $delayInSeconds
        |                   Affected Roads: $affectedRoads
        |               }
        """.trimMargin()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrafficIncident) return false

        if (trafficModelId != other.trafficModelId) return false
        if (id != other.id) return false
        if (iconLat != other.iconLat) return false
        if (iconLon != other.iconLon) return false
        if (incidentCategory != other.incidentCategory) return false
        if (magnitudeOfDelay != other.magnitudeOfDelay) return false
        if (clusterSize != other.clusterSize) return false
        if (description != other.description) return false
        if (causeOfAccident != other.causeOfAccident) return false
        if (from != other.from) return false
        if (to != other.to) return false
        if (length != other.length) return false
        if (delayInSeconds != other.delayInSeconds) return false
        if (affectedRoads != other.affectedRoads) return false
        if (incidentCategoryDesc != other.incidentCategoryDesc) return false
        if (magnitudeOfDelayDesc != other.magnitudeOfDelayDesc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = trafficModelId.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + iconLat.hashCode()
        result = 31 * result + iconLon.hashCode()
        result = 31 * result + incidentCategory
        result = 31 * result + magnitudeOfDelay
        result = 31 * result + clusterSize
        result = 31 * result + description.hashCode()
        result = 31 * result + causeOfAccident.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + length
        result = 31 * result + delayInSeconds
        result = 31 * result + affectedRoads.hashCode()
        result = 31 * result + incidentCategoryDesc.hashCode()
        result = 31 * result + magnitudeOfDelayDesc.hashCode()
        return result
    }

    companion object {

        //Incident Category Constants
        val UNKNOWN = 0
        val ACCIDENT = 1
        val FOG = 2
        val DANGEROUS_CONDITIONS = 3
        val RAIN = 4
        val ICE = 5
        val JAM = 6
        val LANE_CLOSED = 7
        val ROAD_CLOSED = 8
        val ROAD_WORKS = 9
        val WIND = 10
        val FLOODING = 11
        val DETOUR = 12
        val CLUSTER = 13

        //Magnitude of Delay Constants
        val UNKNOWN_DELAY = 0 //shown as grey on traffic tiles
        val MINOR = 1 //shown as orange on traffic tiles
        val MODERATE = 2 //shown as light red on traffic tiles
        val MAJOR = 3 //shown as dark red on traffic tiles
        val UNDEFINED = 4 //used for road closures and other indefinite delays - shown as grey on traffic tiles
    }

}

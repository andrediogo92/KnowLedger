package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.bytes
import pt.um.lei.masb.blockchain.utils.flattenBytes
import java.math.BigDecimal

/**
 *
 * Traffic Incidents (https://developer.tomtom.com/online-traffic/online-traffic-documentation-online-traffic-incidents/traffic-incident-details):
 * https://api.tomtom.com/traffic/services/4/incidentDetails/s3/41.506531,-8.451247,41.574115,-8.371253/11/1526494296871/xml?key=<API_KEY>&projection=EPSG4326&language=en&expandCluster=true
 *
 *
 **/
class TrafficIncident(
    //Current traffic model.
    var trafficModelId: String,
    //ID of the traffic incident.
    var id: String,
    //The point where an icon of the cluster or raw incident should be drawn.
    var iconLat: Double,
    //The point where an icon of the cluster or raw incident should be drawn.
    var iconLon: Double,
    //The category associated with this incident. Values are numbers in the range 0-13.
    var incidentCategory: Int,
    //The magnitude of delay associated with the incident.
    var magnitudeOfDelay: Int,
    //The number of incidents in the cluster.
    var clusterSize: Int,
    //Description of the incident in the requested language.
    var description: String,
    //Cause of the incident, where available, in the requested language.
    var causeOfAccident: String,
    //The name of the intersection or location where the traffic due to the incident starts.
    var from: String,
    //The name of the intersection or location where the traffic due to the incident ends.
    var to: String,
    //Length of the incident in meters.
    var length: Int,
    //Delay caused by the incident in seconds (except in road closures).
    var delayInSeconds: Int,
    //The road number/s affected by the incident. Multiple road numbers will delimited by slashes.
    var affectedRoads: String,
    city: String = "TBD",
    citySeqNum: Int = 1
) : AbstractTrafficIncident(
    city,
    citySeqNum
) {
    override fun calculateDiff(previous: SelfInterval): BigDecimal {
        TODO("calculateDiff not implemented")
    }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            flattenBytes(
                trafficModelId.toByteArray(),
                id.toByteArray(),
                iconLat.bytes(),
                iconLon.bytes(),
                incidentCategory.bytes(),
                magnitudeOfDelay.bytes(),
                clusterSize.bytes(),
                description.toByteArray(),
                causeOfAccident.toByteArray(),
                from.toByteArray(),
                to.toByteArray(),
                length.bytes(),
                delayInSeconds.bytes(),
                affectedRoads.toByteArray(),
                cityName.toByteArray(),
                citySeqNum.bytes()
            )
        )

    override fun store(session: NewInstanceSession): OElement =
        session.newInstance("TrafficFlow").let {
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
            IncidentCategory.Cluster.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.Cluster.toString()
            IncidentCategory.Detour.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.Detour.toString()
            IncidentCategory.Flooding.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.Flooding.toString()
            IncidentCategory.Wind.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.Wind.toString()
            IncidentCategory.RoadWorks.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.RoadWorks.toString()
            IncidentCategory.RoadClosed.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.RoadClosed.toString()
            IncidentCategory.LaneClosed.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.LaneClosed.toString()
            IncidentCategory.Jam.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.Jam.toString()
            IncidentCategory.Ice.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.Ice.toString()
            IncidentCategory.Rain.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.Rain.toString()
            IncidentCategory.DangerousConditions.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.DangerousConditions.toString()
            IncidentCategory.Fog.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.Fog.toString()
            IncidentCategory.Accident.ordinal ->
                this.incidentCategoryDesc = IncidentCategory.Accident.toString()
            else ->
                this.incidentCategoryDesc = IncidentCategory.Unknown.toString()
        }
        when (this.magnitudeOfDelay) {
            MagnitudeDelay.Undefined.ordinal ->
                this.magnitudeOfDelayDesc = MagnitudeDelay.Undefined.toString()
            MagnitudeDelay.Major.ordinal ->
                this.magnitudeOfDelayDesc = MagnitudeDelay.Major.toString()
            MagnitudeDelay.Moderate.ordinal ->
                this.magnitudeOfDelayDesc = MagnitudeDelay.Moderate.toString()
            MagnitudeDelay.Minor.ordinal ->
                this.magnitudeOfDelayDesc = MagnitudeDelay.Minor.toString()
            else -> {
                this.magnitudeOfDelayDesc = MagnitudeDelay.UnknownDelay.toString()
            }
        }
    }


    override fun toString(): String =
        """
        |TrafficIncident {
        |                   Id: $id,
        |                   Traffic Model Id: $id,
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

    companion object : KLogging()
}

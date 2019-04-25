package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import mu.KLogging
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
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
@JsonClass(generateAdapter = true)
class TrafficIncidentData(
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
        session
            .newInstance("TrafficFlowData")
            .apply {
                setProperty("trafficModelId", trafficModelId)
                setProperty("id", id)
                setProperty("iconLat", iconLat)
                setProperty("iconLon", iconLon)
                setProperty("incidentCategory", incidentCategory)
                setProperty("magnitudeOfDelay", magnitudeOfDelay)
                setProperty("clusterSize", clusterSize)
                setProperty("description", description)
                setProperty("causeOfAccident", causeOfAccident)
                setProperty("from", from)
                setProperty("to", to)
                setProperty("length", length)
                setProperty("delayInSeconds", delayInSeconds)
                setProperty("affectedRoads", affectedRoads)
                setProperty("cityName", cityName)
                setProperty("citySeqNum", citySeqNum)
            }


    //The category description associated with this incident.
    var incidentCategoryDesc: String = when (incidentCategory) {
        IncidentCategory.Cluster.ordinal ->
            IncidentCategory.Cluster.toString()
        IncidentCategory.Detour.ordinal ->
            IncidentCategory.Detour.toString()
        IncidentCategory.Flooding.ordinal ->
            IncidentCategory.Flooding.toString()
        IncidentCategory.Wind.ordinal ->
            IncidentCategory.Wind.toString()
        IncidentCategory.RoadWorks.ordinal ->
            IncidentCategory.RoadWorks.toString()
        IncidentCategory.RoadClosed.ordinal ->
            IncidentCategory.RoadClosed.toString()
        IncidentCategory.LaneClosed.ordinal ->
            IncidentCategory.LaneClosed.toString()
        IncidentCategory.Jam.ordinal ->
            IncidentCategory.Jam.toString()
        IncidentCategory.Ice.ordinal ->
            IncidentCategory.Ice.toString()
        IncidentCategory.Rain.ordinal ->
            IncidentCategory.Rain.toString()
        IncidentCategory.DangerousConditions.ordinal ->
            IncidentCategory.DangerousConditions.toString()
        IncidentCategory.Fog.ordinal ->
            IncidentCategory.Fog.toString()
        IncidentCategory.Accident.ordinal ->
            IncidentCategory.Accident.toString()
        else ->
            IncidentCategory.Unknown.toString()
    }

    //The magnitude of delay description associated with the incident.
    var magnitudeOfDelayDesc: String = when (magnitudeOfDelay) {
        MagnitudeDelay.Undefined.ordinal ->
            MagnitudeDelay.Undefined.toString()
        MagnitudeDelay.Major.ordinal ->
            MagnitudeDelay.Major.toString()
        MagnitudeDelay.Moderate.ordinal ->
            MagnitudeDelay.Moderate.toString()
        MagnitudeDelay.Minor.ordinal ->
            MagnitudeDelay.Minor.toString()
        else -> {
            MagnitudeDelay.UnknownDelay.toString()
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrafficIncidentData) return false

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

    override fun toString(): String {
        return "TrafficIncidentData(trafficModelId='$trafficModelId', id='$id', iconLat=$iconLat, iconLon=$iconLon, incidentCategory=$incidentCategory, magnitudeOfDelay=$magnitudeOfDelay, clusterSize=$clusterSize, description='$description', causeOfAccident='$causeOfAccident', from='$from', to='$to', length=$length, delayInSeconds=$delayInSeconds, affectedRoads='$affectedRoads', incidentCategoryDesc='$incidentCategoryDesc', magnitudeOfDelayDesc='$magnitudeOfDelayDesc')"
    }

    companion object : KLogging()
}

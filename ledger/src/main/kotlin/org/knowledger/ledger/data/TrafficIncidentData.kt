package org.knowledger.ledger.data

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.SelfInterval
import java.io.InvalidClassException
import java.math.BigDecimal

/**
 *
 * Traffic Incidents (https://developer.tomtom.com/online-traffic/online-traffic-documentation-online-traffic-incidents/traffic-incident-details):
 * https://api.tomtom.com/traffic/services/4/incidentDetails/s3/41.506531,-8.451247,41.574115,-8.371253/11/1526494296871/xml?key=<API_KEY>&projection=EPSG4326&language=en&expandCluster=true
 *
 *
 **/
@Serializable
@SerialName("TrafficIncidentData")
data class TrafficIncidentData(
    //Current traffic model.
    val trafficModelId: String,
    //ID of the traffic incident.
    val id: String,
    //The point where an icon of the cluster or raw incident should be drawn.
    val iconLat: Double,
    //The point where an icon of the cluster or raw incident should be drawn.
    val iconLon: Double,
    //The category associated with this incident. Values are numbers in the range 0-13.
    val incidentCategory: Int,
    //The magnitude of delay associated with the incident.
    val magnitudeOfDelay: Int,
    //The number of incidents in the cluster.
    val clusterSize: Int,
    //Description of the incident in the requested language.
    val description: String,
    //Cause of the incident, where available, in the requested language.
    val causeOfAccident: String,
    //The name of the intersection or location where the traffic due to the incident starts.
    val from: String,
    //The name of the intersection or location where the traffic due to the incident ends.
    val to: String,
    //Length of the incident in meters.
    val length: Int,
    //Delay caused by the incident in seconds (except in road closures).
    val delayInSeconds: Int,
    //The road number/s affected by the incident. Multiple road numbers will delimited by slashes.
    val affectedRoads: String,
    val city: String = "TBD",
    val citySeqNum: Int = 1
) : LedgerData {
    //The category description associated with this incident.
    val incidentCategoryDesc: String =
        when (incidentCategory) {
            IncidentCategory.Cluster.ordinal ->
                IncidentCategory.Cluster.name
            IncidentCategory.Detour.ordinal ->
                IncidentCategory.Detour.name
            IncidentCategory.Flooding.ordinal ->
                IncidentCategory.Flooding.name
            IncidentCategory.Wind.ordinal ->
                IncidentCategory.Wind.name
            IncidentCategory.RoadWorks.ordinal ->
                IncidentCategory.RoadWorks.name
            IncidentCategory.RoadClosed.ordinal ->
                IncidentCategory.RoadClosed.name
            IncidentCategory.LaneClosed.ordinal ->
                IncidentCategory.LaneClosed.name
            IncidentCategory.Jam.ordinal ->
                IncidentCategory.Jam.name
            IncidentCategory.Ice.ordinal ->
                IncidentCategory.Ice.name
            IncidentCategory.Rain.ordinal ->
                IncidentCategory.Rain.name
            IncidentCategory.DangerousConditions.ordinal ->
                IncidentCategory.DangerousConditions.name
            IncidentCategory.Fog.ordinal ->
                IncidentCategory.Fog.name
            IncidentCategory.Accident.ordinal ->
                IncidentCategory.Accident.name
            else ->
                IncidentCategory.Unknown.name
        }

    //The magnitude of delay description associated with the incident.
    val magnitudeOfDelayDescription: String =
        when (magnitudeOfDelay) {
            MagnitudeDelay.Undefined.ordinal ->
                MagnitudeDelay.Undefined.name
            MagnitudeDelay.Major.ordinal ->
                MagnitudeDelay.Major.name
            MagnitudeDelay.Moderate.ordinal ->
                MagnitudeDelay.Moderate.name
            MagnitudeDelay.Minor.ordinal ->
                MagnitudeDelay.Minor.name
            else -> {
                MagnitudeDelay.UnknownDelay.name
            }
        }

    override fun clone(): TrafficIncidentData = copy()


    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)


    override fun calculateDiff(previous: SelfInterval): BigDecimal {
        return if (previous is TrafficIncidentData) {
            calculateDiffTraffic(previous)
        } else {
            throw InvalidClassException(
                """SelfInterval supplied is:
                    |   ${previous.javaClass.name},
                    |   not ${this::class.java.name}
                """.trimMargin()
            )
        }
    }

    private fun calculateDiffTraffic(previous: TrafficIncidentData): BigDecimal {
        return BigDecimal.ONE
    }
}

package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.TrafficIncidentData
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.LedgerData

class TrafficIncidentDataStorageAdapter(hashers: Hashers) :
    AbstractStorageAdapter<TrafficIncidentData>(TrafficIncidentData::class, hashers) {
    override val serializer: KSerializer<TrafficIncidentData> get() = TrafficIncidentData.serializer()

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "trafficModelId" to StorageType.STRING,
            "id" to StorageType.INTEGER,
            "iconLat" to StorageType.DOUBLE,
            "iconLon" to StorageType.DOUBLE,
            "incidentCategory" to StorageType.INTEGER,
            "magnitudeOfDelay" to StorageType.INTEGER,
            "clusterSize" to StorageType.INTEGER,
            "description" to StorageType.STRING,
            "causeOfAccident" to StorageType.STRING,
            "from" to StorageType.STRING,
            "to" to StorageType.STRING,
            "length" to StorageType.INTEGER,
            "delayInSeconds" to StorageType.INTEGER,
            "affectedRoads" to StorageType.STRING,
            "city" to StorageType.STRING,
            "citySeqNum" to StorageType.INTEGER
        )

    override fun store(toStore: LedgerData, session: NewInstanceSession): StorageElement =
        (toStore as TrafficIncidentData).let { trafficIncidentData ->
            session.newInstance(id)
                .setStorageProperty("trafficModelId", trafficIncidentData.trafficModelId)
                .setStorageProperty("id", trafficIncidentData.id)
                .setStorageProperty("iconLat", trafficIncidentData.iconLat)
                .setStorageProperty("iconLon", trafficIncidentData.iconLon)
                .setStorageProperty("incidentCategory", trafficIncidentData.incidentCategory)
                .setStorageProperty("magnitudeOfDelay", trafficIncidentData.magnitudeOfDelay)
                .setStorageProperty("clusterSize", trafficIncidentData.clusterSize)
                .setStorageProperty("description", trafficIncidentData.description)
                .setStorageProperty("causeOfAccident", trafficIncidentData.causeOfAccident)
                .setStorageProperty("from", trafficIncidentData.from)
                .setStorageProperty("to", trafficIncidentData.to)
                .setStorageProperty("length", trafficIncidentData.length)
                .setStorageProperty("delayInSeconds", trafficIncidentData.delayInSeconds)
                .setStorageProperty("affectedRoads", trafficIncidentData.affectedRoads)
                .setStorageProperty("city", trafficIncidentData.city)
                .setStorageProperty("citySeqNum", trafficIncidentData.citySeqNum)
        }


    override fun load(element: StorageElement): Outcome<TrafficIncidentData, DataFailure> =
        commonLoad(element, id) {
            TrafficIncidentData(
                getStorageProperty("trafficModelId"),
                getStorageProperty("id"),
                getStorageProperty("iconLat"),
                getStorageProperty("iconLon"),
                getStorageProperty("incidentCategory"),
                getStorageProperty("magnitudeOfDelay"),
                getStorageProperty("clusterSize"),
                getStorageProperty("description"),
                getStorageProperty("causeOfAccident"),
                getStorageProperty("from"),
                getStorageProperty("to"),
                getStorageProperty("length"),
                getStorageProperty("delayInSeconds"),
                getStorageProperty("affectedRoads"),
                getStorageProperty("city"),
                getStorageProperty("citySeqNum")
            ).ok()
        }
}
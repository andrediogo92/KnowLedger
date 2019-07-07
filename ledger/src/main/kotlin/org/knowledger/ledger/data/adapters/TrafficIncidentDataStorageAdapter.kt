package org.knowledger.ledger.data.adapters

import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.adapters.AbstractStorageAdapter
import org.knowledger.common.storage.results.DataFailure
import org.knowledger.ledger.data.TrafficIncidentData

object TrafficIncidentDataStorageAdapter : AbstractStorageAdapter<TrafficIncidentData>(
    TrafficIncidentData::class.java
) {
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

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement =
        (toStore as TrafficIncidentData).let {
            session
                .newInstance(id)
                .setStorageProperty(
                    "trafficModelId",
                    it.trafficModelId
                ).setStorageProperty("id", it.id)
                .setStorageProperty(
                    "iconLat", it.iconLat
                ).setStorageProperty(
                    "iconLon", it.iconLon
                ).setStorageProperty(
                    "incidentCategory",
                    it.incidentCategory
                ).setStorageProperty(
                    "magnitudeOfDelay",
                    it.magnitudeOfDelay
                ).setStorageProperty(
                    "clusterSize", it.clusterSize
                ).setStorageProperty(
                    "description", it.description
                ).setStorageProperty(
                    "causeOfAccident",
                    it.causeOfAccident
                ).setStorageProperty("from", it.from)
                .setStorageProperty("to", it.to)
                .setStorageProperty("length", it.length)
                .setStorageProperty(
                    "delayInSeconds", it.delayInSeconds
                ).setStorageProperty(
                    "affectedRoads", it.affectedRoads
                ).setStorageProperty(
                    "cityName", it.cityName
                ).setStorageProperty(
                    "citySeqNum", it.citySeqNum
                )

        }


    override fun load(
        element: StorageElement
    ): Outcome<TrafficIncidentData, DataFailure> =
        commonLoad(element, id) {
            Outcome.Ok(
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
                    getStorageProperty("cityName"),
                    getStorageProperty("citySeqNum")
                )
            )
        }
}
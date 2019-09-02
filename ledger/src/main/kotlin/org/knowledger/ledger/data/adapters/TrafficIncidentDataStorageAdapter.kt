package org.knowledger.ledger.data.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.crypto.hash.AvailableHashAlgorithms
import org.knowledger.ledger.data.TrafficIncidentData

object TrafficIncidentDataStorageAdapter : AbstractStorageAdapter<TrafficIncidentData>(
    TrafficIncidentData::class.java,
    AvailableHashAlgorithms.SHA3512Hasher
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
                    "city", it.city
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
                    getStorageProperty("city"),
                    getStorageProperty("citySeqNum")
                )
            )
        }
}
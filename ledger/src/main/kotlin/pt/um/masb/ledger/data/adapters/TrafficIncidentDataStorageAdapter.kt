package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.ledger.data.TrafficIncidentData

class TrafficIncidentDataStorageAdapter : AbstractStorageAdapter<TrafficIncidentData>(
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
        toStore: BlockChainData, session: NewInstanceSession
    ): StorageElement {
        val trafficIncident = toStore as TrafficIncidentData
        return session.newInstance(id).apply {
            setStorageProperty(
                "trafficModelId",
                trafficIncident.trafficModelId
            )
            setStorageProperty("id", trafficIncident.id)
            setStorageProperty("iconLat", trafficIncident.iconLat)
            setStorageProperty("iconLon", trafficIncident.iconLon)
            setStorageProperty(
                "incidentCategory",
                trafficIncident.incidentCategory
            )
            setStorageProperty(
                "magnitudeOfDelay",
                trafficIncident.magnitudeOfDelay
            )
            setStorageProperty("clusterSize", trafficIncident.clusterSize)
            setStorageProperty("description", trafficIncident.description)
            setStorageProperty(
                "causeOfAccident",
                trafficIncident.causeOfAccident
            )
            setStorageProperty("from", trafficIncident.from)
            setStorageProperty("to", trafficIncident.to)
            setStorageProperty("length", trafficIncident.length)
            setStorageProperty(
                "delayInSeconds",
                trafficIncident.delayInSeconds
            )
            setStorageProperty(
                "affectedRoads",
                trafficIncident.affectedRoads
            )
            setStorageProperty("cityName", trafficIncident.cityName)
            setStorageProperty("citySeqNum", trafficIncident.citySeqNum)
        }
    }

    override fun load(
        element: StorageElement
    ): DataResult<TrafficIncidentData> =
        commonLoad(element, id) {
            DataResult.Success(
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
package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.ledger.data.TrafficFlowData

class TrafficFlowDataStorageAdapter : AbstractStorageAdapter<TrafficFlowData>(
    TrafficFlowData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "functionalRoadClass" to StorageType.STRING,
            "currentSpeed" to StorageType.INTEGER,
            "freeFlowSpeed" to StorageType.INTEGER,
            "currentTravelTime" to StorageType.INTEGER,
            "freeFlowTravelTime" to StorageType.INTEGER,
            "confidence" to StorageType.DOUBLE,
            "realtimeRatio" to StorageType.DOUBLE,
            "city" to StorageType.STRING,
            "citySeqNum" to StorageType.INTEGER
        )

    override fun store(
        toStore: BlockChainData, session: NewInstanceSession
    ): StorageElement {
        val trafficFlowData = toStore as TrafficFlowData
        return session.newInstance(id).apply {
            setStorageProperty(
                "functionalRoadClass",
                trafficFlowData.functionalRoadClass
            )
            setStorageProperty(
                "currentSpeed",
                trafficFlowData.currentSpeed
            )
            setStorageProperty(
                "freeFlowSpeed",
                trafficFlowData.freeFlowSpeed
            )
            setStorageProperty(
                "currentTravelTime",
                trafficFlowData.currentTravelTime
            )
            setStorageProperty(
                "freeFlowTravelTime",
                trafficFlowData.freeFlowTravelTime
            )
            setStorageProperty(
                "confidence",
                trafficFlowData.confidence
            )
            setStorageProperty(
                "realtimeRatio",
                trafficFlowData.realtimeRatio
            )
            setStorageProperty(
                "cityName", trafficFlowData.cityName
            )
            setStorageProperty(
                "citySeqNum", trafficFlowData.citySeqNum
            )
        }
    }

    override fun load(element: StorageElement): DataResult<TrafficFlowData> =
        commonLoad(element, id) {
            DataResult.Success(
                TrafficFlowData(
                    getStorageProperty("functionalRoadClass"),
                    getStorageProperty("currentSpeed"),
                    getStorageProperty("freeFlowSpeed"),
                    getStorageProperty("currentTravelTime"),
                    getStorageProperty("freeFlowTravelTime"),
                    getStorageProperty("confidence"),
                    getStorageProperty("realtimeRatio"),
                    getStorageProperty("cityName"),
                    getStorageProperty("citySeqNum")
                )
            )
        }
}
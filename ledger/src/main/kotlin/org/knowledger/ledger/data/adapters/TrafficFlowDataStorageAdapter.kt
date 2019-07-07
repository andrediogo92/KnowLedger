package org.knowledger.ledger.data.adapters

import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.adapters.AbstractStorageAdapter
import org.knowledger.common.storage.results.DataFailure
import org.knowledger.ledger.data.TrafficFlowData

object TrafficFlowDataStorageAdapter : AbstractStorageAdapter<TrafficFlowData>(
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
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement =
        (toStore as TrafficFlowData).let {
            session
                .newInstance(id)
                .setStorageProperty(
                    "functionalRoadClass",
                    it.functionalRoadClass
                ).setStorageProperty(
                    "currentSpeed",
                    it.currentSpeed
                ).setStorageProperty(
                    "freeFlowSpeed",
                    it.freeFlowSpeed
                ).setStorageProperty(
                    "currentTravelTime",
                    it.currentTravelTime
                ).setStorageProperty(
                    "freeFlowTravelTime",
                    it.freeFlowTravelTime
                ).setStorageProperty(
                    "confidence",
                    it.confidence
                ).setStorageProperty(
                    "realtimeRatio",
                    it.realtimeRatio
                ).setStorageProperty(
                    "cityName", it.cityName
                ).setStorageProperty(
                    "citySeqNum", it.citySeqNum
                )

        }

    override fun load(
        element: StorageElement
    ): Outcome<TrafficFlowData, DataFailure> =
        commonLoad(element, id) {
            Outcome.Ok(
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
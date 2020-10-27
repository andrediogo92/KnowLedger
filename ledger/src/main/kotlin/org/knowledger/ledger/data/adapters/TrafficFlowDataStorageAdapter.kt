package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.TrafficFlowData
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.LedgerData

class TrafficFlowDataStorageAdapter(hashers: Hashers) :
    AbstractStorageAdapter<TrafficFlowData>(TrafficFlowData::class, hashers) {
    override val serializer: KSerializer<TrafficFlowData> get() = TrafficFlowData.serializer()

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

    override fun store(toStore: LedgerData, session: NewInstanceSession): StorageElement =
        (toStore as TrafficFlowData).let { trafficFlowData ->
            session.newInstance(id)
                .setStorageProperty("functionalRoadClass", trafficFlowData.functionalRoadClass)
                .setStorageProperty("currentSpeed", trafficFlowData.currentSpeed)
                .setStorageProperty("freeFlowSpeed", trafficFlowData.freeFlowSpeed)
                .setStorageProperty("currentTravelTime", trafficFlowData.currentTravelTime)
                .setStorageProperty("freeFlowTravelTime", trafficFlowData.freeFlowTravelTime)
                .setStorageProperty("confidence", trafficFlowData.confidence)
                .setStorageProperty("realtimeRatio", trafficFlowData.realtimeRatio)
                .setStorageProperty("city", trafficFlowData.city)
                .setStorageProperty("citySeqNum", trafficFlowData.citySeqNum)
        }

    override fun load(element: StorageElement): Outcome<TrafficFlowData, DataFailure> =
        commonLoad(element, id) {
            TrafficFlowData(
                getStorageProperty("functionalRoadClass"),
                getStorageProperty("currentSpeed"),
                getStorageProperty("freeFlowSpeed"),
                getStorageProperty("currentTravelTime"),
                getStorageProperty("freeFlowTravelTime"),
                getStorageProperty("confidence"),
                getStorageProperty("realtimeRatio"),
                getStorageProperty("city"),
                getStorageProperty("citySeqNum")
            ).ok()
        }
}
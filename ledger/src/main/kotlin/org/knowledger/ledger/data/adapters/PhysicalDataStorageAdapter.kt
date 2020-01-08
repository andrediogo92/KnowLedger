package org.knowledger.ledger.data.adapters

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.data.GeoCoords
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.adapters.StorageAdapterNotRegistered
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.intoLoad
import org.knowledger.ledger.results.mapFailure
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import java.time.Instant

object PhysicalDataStorageAdapter : LedgerStorageAdapter<PhysicalData> {
    override val id: String
        get() = "PhysicalData"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "seconds" to StorageType.LONG,
            "nanos" to StorageType.INTEGER,
            "value" to StorageType.LINK,
            "latitude" to StorageType.DECIMAL,
            "longitude" to StorageType.DECIMAL,
            "altitude" to StorageType.DECIMAL
        )

    override fun store(
        toStore: PhysicalData, session: ManagedSession
    ): StorageElement =
        LedgerHandle.getStorageAdapter(
            toStore.data.javaClass
        )?.let {
            session
                .newInstance(id).setStorageProperty(
                    "seconds", toStore.instant.epochSecond
                ).setStorageProperty(
                    "nanos", toStore.instant.nano
                ).setLinked(
                    "value", it,
                    toStore.data, session
                ).setStorageProperty("latitude", toStore.coords.latitude)
                .setStorageProperty("longitude", toStore.coords.longitude)
                .setStorageProperty("altitude", toStore.coords.altitude)
        } ?: throw StorageAdapterNotRegistered()


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<PhysicalData, LoadFailure> =
        tryOrLoadUnknownFailure {
            val dataElem = element.getLinked("value")
            val dataName = dataElem.schema
            val loader = dataName?.let {
                LedgerHandle.getStorageAdapter(dataName)
            }
            if (dataName != null && loader != null) {
                loader
                    .load(dataElem)
                    .mapFailure {
                        it.intoLoad()
                    }.mapSuccess {
                        val instant = Instant.ofEpochSecond(
                            element.getStorageProperty("seconds"),
                            element.getStorageProperty("nanos")
                        )
                        PhysicalData(
                            instant,
                            GeoCoords(
                                element.getStorageProperty("latitude"),
                                element.getStorageProperty("longitude"),
                                element.getStorageProperty("altitude")
                            ),
                            it
                        )
                    }
            } else {
                Outcome.Error<LoadFailure>(
                    LoadFailure.UnrecognizedDataType(
                        "Data property was unrecognized in physical value loader: $dataElem"
                    )
                )
            }
        }
}
package org.knowledger.ledger.adapters.storage

import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewLinked
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.GeoCoords
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.intoLoad
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class PhysicalDataStorageAdapter : LedgerStorageAdapter<PhysicalData> {
    override val id: String
        get() = "PhysicalData"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "millis" to StorageType.LONG,
            "value" to StorageType.LINK,
            "latitude" to StorageType.DECIMAL,
            "longitude" to StorageType.DECIMAL,
            "altitude" to StorageType.DECIMAL
        )

    override fun store(
        element: PhysicalData, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewNative("millis", element.millis)
                pushNewLinked("value", element.data, AdapterIds.LedgerData)
                pushNewNative("latitude", element.coords.latitude)
                pushNewNative("longitude", element.coords.longitude)
                pushNewNative("altitude", element.coords.altitude)
            }.ok()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<PhysicalData, LoadFailure> =
        tryOrLoadUnknownFailure {
            val dataElem = element.getLinked("value")
            val dataName = dataElem.schema
            val loader = dataName?.let {
                context.findAdapter(dataName)
            }
            if (dataName != null && loader != null) {
                loader.load(dataElem).mapError {
                    it.intoLoad()
                }.map { ledgerData ->
                    val millis: Long =
                        element.getStorageProperty("millis")

                    PhysicalData(
                        millis, GeoCoords(
                            element.getStorageProperty("latitude"),
                            element.getStorageProperty("longitude"),
                            element.getStorageProperty("altitude")
                        ), ledgerData
                    )
                }
            } else {
                LoadFailure.UnrecognizedDataType(
                    "Data property was unrecognized in physical data loader: $dataName"
                ).err()
            }
        }
}
package org.knowledger.ledger.adapters.storage

import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.core.adapters.Tag
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.GeoCoords
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.intoLoad
import java.math.BigDecimal

internal class PhysicalDataStorageAdapter : LedgerStorageAdapter<PhysicalData> {
    override val id: String get() = "PhysicalData"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "millis" to StorageType.LONG,
            "ledgerData" to StorageType.LINK,
            "latitude" to StorageType.DECIMAL,
            "longitude" to StorageType.DECIMAL,
            "altitude" to StorageType.DECIMAL,
        )

    override fun store(element: PhysicalData, state: StorageState): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewNative("millis", element.millis)
            pushNewLinked("ledgerData", element.data, AdapterIds.LedgerData)
            pushNewNative("latitude", element.coords.latitude)
            pushNewNative("longitude", element.coords.longitude)
            pushNewNative("altitude", element.coords.altitude)
        }.ok()


    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<PhysicalData, LoadFailure> =
        with(element) {
            val dataElem = getLinked("ledgerData")
            val dataName = dataElem.schema
            val loader = dataName?.let { context.findAdapter(Tag(dataName)) }
            if (dataName != null && loader != null) {
                loader.load(dataElem).mapError(DataFailure::intoLoad).map { ledgerData ->
                    val millis: Long = getStorageProperty("millis")
                    val latitude: BigDecimal = getStorageProperty("latitude")
                    val longitude: BigDecimal = getStorageProperty("longitude")
                    val altitude: BigDecimal = getStorageProperty("altitude")
                    PhysicalData(millis, GeoCoords(latitude, longitude, altitude), ledgerData)
                }
            } else {
                LoadFailure.UnrecognizedDataType(
                    "Data property was unrecognized in physical data loader: $dataName"
                ).err()
            }
        }
}
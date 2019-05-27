package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.adapters.StorageAdapterNotRegistered
import pt.um.masb.ledger.data.GeoCoords
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.results.intoLoad
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.adapters.LedgerStorageAdapter
import java.time.Instant

object PhysicalDataStorageAdapter : LedgerStorageAdapter<PhysicalData> {
    override val id: String
        get() = "PhysicalData"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "seconds" to StorageType.LONG,
            "nanos" to StorageType.INTEGER,
            "data" to StorageType.LINK
        )

    override fun store(
        toStore: PhysicalData, session: NewInstanceSession
    ): StorageElement {
        val dataStorageAdapter =
            LedgerHandle.getStorageAdapter(
                toStore.data.javaClass
            ) ?: throw StorageAdapterNotRegistered()

        return session.newInstance(id).apply {
            setStorageProperty(
                "seconds", toStore.instant.epochSecond
            )
            setStorageProperty(
                "nanos", toStore.instant.nano
            )
            setLinked(
                "data", dataStorageAdapter,
                toStore.data, session
            )
            toStore.geoCoords?.let {
                setStorageProperty("latitude", it.latitude)
                setStorageProperty("longitude", it.longitude)
                setStorageProperty("altitude", it.altitude)
            }
        }

    }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<PhysicalData, LoadFailure> =
        tryOrLoadUnknownFailure {
            val dataElem = element.getLinked("data")
            val dataName = dataElem.schema
            val loader = dataName?.let {
                LedgerHandle.getStorageAdapter(dataName)
            }
            if (dataName != null && loader != null) {
                loader.load(dataElem).flatMapSuccess {
                    val instant = Instant.ofEpochSecond(
                        element.getStorageProperty("seconds"),
                        element.getStorageProperty("nanos")
                    )
                    if (element.presentProperties.contains("latitude")) {
                        PhysicalData(
                            instant,
                            GeoCoords(
                                element.getStorageProperty("latitude"),
                                element.getStorageProperty("longitude"),
                                element.getStorageProperty("altitude")
                            ),
                            this
                        )

                    } else {
                        PhysicalData(
                            instant,
                            this
                        )
                    }
                }.mapError {
                    Outcome.Error<PhysicalData, LoadFailure>(this.failure.intoLoad())
                }
            } else {
                Outcome.Error<PhysicalData, LoadFailure>(
                    LoadFailure.UnrecognizedDataType(
                        "Data property was unrecognized in physical data loader: $dataElem"
                    )
                )
            }
        }
}
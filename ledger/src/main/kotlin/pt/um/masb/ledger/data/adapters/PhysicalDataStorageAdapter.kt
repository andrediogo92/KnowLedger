package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.storage.adapters.NotBaseClassException
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.ledger.data.GeoCoords
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.results.intoLoad
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.LedgerService
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.adapters.LedgerStorageAdapter
import java.time.Instant

class PhysicalDataStorageAdapter : LedgerStorageAdapter<PhysicalData> {
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
            LedgerService.getStorageAdapter(
                toStore.data.javaClass
            ) ?: throw NotBaseClassException()

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
        hash: Hash, element: StorageElement
    ): LoadResult<PhysicalData> =
        tryOrLoadQueryFailure {
            val dataElem = element.getLinked("data")
            val dataName = dataElem.schema
            val loader = dataName?.let {
                LedgerService.getStorageAdapter(dataName)
            }
            if (dataName != null && loader != null) {
                val data = loader.load(dataElem)
                if (data !is DataResult.Success) {
                    return@tryOrLoadQueryFailure data.intoLoad<PhysicalData>()
                }
                val instant = Instant.ofEpochSecond(
                    element.getStorageProperty("seconds"),
                    element.getStorageProperty("nanos")
                )
                if (element.presentProperties.contains("latitude")) {
                    LoadResult.Success(
                        PhysicalData(
                            instant,
                            GeoCoords(
                                element.getStorageProperty("latitude"),
                                element.getStorageProperty("longitude"),
                                element.getStorageProperty("altitude")
                            ),
                            data.data
                        )
                    )
                } else {
                    LoadResult.Success(
                        PhysicalData(
                            instant,
                            data.data
                        )
                    )
                }
            } else {
                LoadResult.UnrecognizedDataType<PhysicalData>(
                    "Data property was unrecognized in physical data loader: $dataElem"
                )
            }
        }
}
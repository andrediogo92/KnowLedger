package pt.um.masb.ledger.config.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.results.intoLoad
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.adapters.LedgerStorageAdapter
import java.time.Instant
import java.util.*

class LedgerIdStorageAdapter : LedgerStorageAdapter<LedgerId> {
    val ledgerParamsStorageAdapter = LedgerParamsStorageAdapter()

    override val id: String
        get() = "LedgerId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "uuid" to StorageType.STRING,
            "timestamp" to StorageType.STRING,
            "id" to StorageType.STRING,
            "hashId" to StorageType.BYTES,
            "params" to StorageType.LINK
        )

    override fun store(
        toStore: LedgerId, session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty(
                "uuid", toStore.uuid.toString()
            )
            setStorageProperty(
                "timestamp", toStore.timestamp.toString()
            )
            setStorageProperty("id", toStore.id)
            setHashProperty("hashId", toStore.hashId)
            setLinked(
                "params", ledgerParamsStorageAdapter,
                toStore.params, session
            )
        }


    override fun load(
        hash: Hash, element: StorageElement
    ): LoadResult<LedgerId> =
        tryOrLoadQueryFailure {
            val uuid = UUID.fromString(
                element.getStorageProperty<String>("uuid")
            )

            val timestamp = Instant.ofEpochSecond(
                element.getStorageProperty("seconds"),
                element.getStorageProperty<Int>("nanos").toLong()
            )

            val id: String =
                element.getStorageProperty("id")

            val ledgerHash =
                element.getHashProperty("hashId")

            val params = ledgerParamsStorageAdapter.load(
                ledgerHash,
                element.getLinked("params")
            )
            params.intoLoad {
                LedgerId(id, uuid, timestamp, this)
            }
        }
}
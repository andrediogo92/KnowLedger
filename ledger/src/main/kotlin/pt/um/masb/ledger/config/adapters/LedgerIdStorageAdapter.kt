package pt.um.masb.ledger.config.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.adapters.LedgerStorageAdapter
import java.time.Instant
import java.util.*

object LedgerIdStorageAdapter : LedgerStorageAdapter<LedgerId> {
    override val id: String
        get() = "LedgerId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "uuid" to StorageType.STRING,
            "timestamp" to StorageType.STRING,
            "id" to StorageType.STRING,
            "hashId" to StorageType.HASH,
            "ledgerParams" to StorageType.LINK
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
                "ledgerParams", LedgerParamsStorageAdapter,
                toStore.params, session
            )
        }


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerId, LoadFailure> =
        tryOrLoadUnknownFailure {
            val uuid = UUID.fromString(
                element.getStorageProperty<String>("uuid")
            )
            val timestamp = Instant.ofEpochSecond(
                element.getStorageProperty("seconds"),
                element.getStorageProperty<Int>("nanos").toLong()
            )
            val hash =
                element.getHashProperty("hashId")

            assert(hash.contentEquals(ledgerHash))

            LedgerParamsStorageAdapter.load(
                ledgerHash,
                element.getLinked("ledgerParams")
            ).flatMapSuccess {
                LedgerId(
                    element.getStorageProperty("id"),
                    uuid,
                    timestamp,
                    this,
                    ledgerHash
                )
            }
        }
}
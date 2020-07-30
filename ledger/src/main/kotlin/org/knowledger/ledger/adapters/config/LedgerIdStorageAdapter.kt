package org.knowledger.ledger.adapters.config

import com.github.michaelbull.result.map
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewHash
import org.knowledger.ledger.service.solver.pushNewLinked
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.config.LedgerId
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure
import java.time.Instant
import java.util.*

internal class LedgerIdStorageAdapter : LedgerStorageAdapter<LedgerId> {
    override val id: String
        get() = "LedgerId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "tag" to StorageType.STRING,
            "hash" to StorageType.HASH,
            "uuid" to StorageType.STRING,
            "instant" to StorageType.STRING,
            "ledgerParams" to StorageType.LINK
        )

    override fun update(
        element: LedgerId, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        store(element, solver)

    override fun store(
        element: LedgerId, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewNative("tag", element.tag)
                pushNewHash("hash", element.hash)
                pushNewNative("uuid", element.uuid.toString())
                pushNewNative("instant", element.instant.toString())
                pushNewLinked("ledgerParams", element.ledgerParams, AdapterIds.LedgerParams)
            }.ok()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<LedgerId, LoadFailure> =
        tryOrLoadUnknownFailure {
            val tag: String = element.getStorageProperty("tag")
            val hash: Hash = element.getHashProperty("hash")
            assert(hash == ledgerHash)
            val uuid: UUID = UUID.fromString(element.getStorageProperty("uuid"))
            val instant: Instant = Instant.parse(element.getStorageProperty("instant"))
            val ledgerParamsElem = element.getLinked("hashers")
            context.ledgerParamsStorageAdapter.load(
                ledgerHash, ledgerParamsElem, context
            ).map { ledgerParams ->
                LedgerId(tag, ledgerHash, uuid, instant, ledgerParams)
            }

        }
}
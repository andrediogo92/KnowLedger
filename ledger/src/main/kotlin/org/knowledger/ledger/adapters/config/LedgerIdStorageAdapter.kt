package org.knowledger.ledger.adapters.config

import com.github.michaelbull.result.map
import org.knowledger.ledger.adapters.service.HandleStorageAdapter
import org.knowledger.ledger.adapters.service.LedgerMagicPair
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.core.Instant
import org.knowledger.ledger.core.UUID
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.config.LedgerId
import org.knowledger.ledger.storage.results.LoadFailure

internal class LedgerIdStorageAdapter : HandleStorageAdapter<LedgerId> {
    override val id: String get() = "LedgerId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "tag" to StorageType.STRING,
            "hash" to StorageType.HASH,
            "uuid" to StorageType.STRING,
            "instant" to StorageType.STRING,
            "ledgerParams" to StorageType.LINK,
        )

    override fun update(element: LedgerId, state: StorageState): Outcome<Unit, DataFailure> =
        store(element, state)

    override fun store(element: LedgerId, state: StorageState): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewNative("tag", element.tag)
            pushNewHash("hash", element.hash)
            pushNewNative("uuid", element.uuid.toString())
            pushNewNative("instant", element.instant.toString())
            pushNewLinked("ledgerParams", element.ledgerParams, AdapterIds.LedgerParams)
        }.ok()


    override fun load(
        ledgerHash: Hash, element: StorageElement, context: LedgerMagicPair,
    ): Outcome<LedgerId, LoadFailure> =
        with(element) {
            val tag: String = getStorageProperty("tag")
            val hash: Hash = getHashProperty("hash")
            assert(hash == ledgerHash)
            val uuid: UUID = UUID.fromString(getStorageProperty("uuid"))
            val instant: Instant = Instant.parse(getStorageProperty("instant"))
            val ledgerParamsElem = getLinked("ledgerParams")
            context.adapter
                .load(ledgerHash, ledgerParamsElem, context)
                .map { ledgerParams ->
                    LedgerId(tag, ledgerHash, uuid, instant, ledgerParams)
                }
        }

}
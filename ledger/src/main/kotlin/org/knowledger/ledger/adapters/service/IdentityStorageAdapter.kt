package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.toPrivateKey
import org.knowledger.ledger.crypto.toPublicKey
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.storage.Identity
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure
import java.security.KeyPair

internal class IdentityStorageAdapter : LedgerStorageAdapter<Identity> {
    override val id: String
        get() = "Identity"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "id" to StorageType.STRING,
            "privateKey" to StorageType.BYTES,
            "publicKey" to StorageType.BYTES
        )

    override fun update(
        element: Identity, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        store(element, solver)

    override fun store(
        element: Identity, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewNative("id", element.id)
                pushNewNative("privateKey", element.privateKey)
                pushNewNative("publicKey", element.publicKey)
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<Identity, LoadFailure> =
        tryOrLoadUnknownFailure {
            val encodedPublicKey = EncodedPublicKey(
                element.getStorageProperty("publicKey")
            )
            val encodedPrivateKey = EncodedPrivateKey(
                element.getStorageProperty("privateKey")
            )
            val keyPair = KeyPair(
                encodedPublicKey.toPublicKey(),
                encodedPrivateKey.toPrivateKey()
            )
            Identity(id, keyPair).ok()
        }
}
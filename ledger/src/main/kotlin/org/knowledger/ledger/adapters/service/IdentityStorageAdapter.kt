package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.EncodedKeyPair
import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.Identity
import org.knowledger.ledger.storage.results.LoadFailure

internal class IdentityStorageAdapter : LedgerStorageAdapter<Identity> {
    override val id: String get() = "Identity"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "id" to StorageType.STRING,
            "privateKey" to StorageType.BYTES,
            "publicKey" to StorageType.BYTES,
        )

    override fun update(element: Identity, state: StorageState): Outcome<Unit, DataFailure> =
        store(element, state)

    override fun store(element: Identity, state: StorageState): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewNative("id", element.id)
            pushNewNative("privateKey", element.privateKey)
            pushNewNative("publicKey", element.publicKey)
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<Identity, LoadFailure> =
        with(element) {
            val encodedPublicKey = EncodedPublicKey(getStorageProperty("publicKey"))
            val encodedPrivateKey = EncodedPrivateKey(getStorageProperty("privateKey"))
            val keyPair = EncodedKeyPair(encodedPublicKey, encodedPrivateKey)
            Identity(id, keyPair).ok()
        }
}

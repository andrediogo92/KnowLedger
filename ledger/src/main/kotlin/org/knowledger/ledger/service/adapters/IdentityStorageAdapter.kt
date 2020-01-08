package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.toPrivateKey
import org.knowledger.ledger.crypto.toPublicKey
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import java.security.KeyPair

internal object IdentityStorageAdapter : LedgerStorageAdapter<Identity> {
    override val id: String
        get() = "Identity"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "id" to StorageType.STRING,
            "privateKey" to StorageType.BYTES,
            "publicKey" to StorageType.BYTES
        )

    override fun store(
        toStore: Identity, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty("id", toStore.id)
            .setStorageProperty(
                "privateKey", toStore.privateKey.encoded
            ).setStorageProperty(
                "publicKey", toStore.publicKey.encoded
            )

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
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
            Outcome.Ok(
                Identity(id, keyPair)
            )
        }
}
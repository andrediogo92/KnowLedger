package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.byteEncodeToPrivateKey
import org.knowledger.ledger.core.misc.byteEncodeToPublicKey
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import java.security.KeyPair

object IdentityStorageAdapter : LedgerStorageAdapter<Identity> {
    override val id: String
        get() = "Identity"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "id" to StorageType.STRING,
            "privateKey" to StorageType.BYTES,
            "publicKey" to StorageType.BYTES
        )

    override fun store(
        toStore: Identity, session: NewInstanceSession
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
            val publicKeyString: ByteArray =
                element.getStorageProperty("publicKey")
            val privateKeyString: ByteArray =
                element.getStorageProperty("privateKey")

            val keyPair = KeyPair(
                publicKeyString.byteEncodeToPublicKey(),
                privateKeyString.byteEncodeToPrivateKey()
            )
            Outcome.Ok(
                Identity(id, keyPair)
            )
        }
}
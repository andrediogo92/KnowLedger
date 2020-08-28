package org.knowledger.ledger.crypto.service

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.data.LedgerContract
import org.knowledger.ledger.crypto.EncodedKeyPair
import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.ecSpec
import org.knowledger.ledger.crypto.keygen
import org.knowledger.ledger.crypto.random
import org.knowledger.ledger.crypto.toEncoded

@Serializable
data class Identity(
    val id: String,
    val privateKey: EncodedPrivateKey,
    val publicKey: EncodedPublicKey,
) : LedgerContract {
    constructor(id: String) : this(id, generateNewKeyPair())

    constructor(
        id: String, keyPair: EncodedKeyPair,
    ) : this(id, keyPair.privateKey, keyPair.publicKey)

    companion object {
        private fun generateNewKeyPair(): EncodedKeyPair =
            with(keygen) {
                // Initialize the key generator and generate a KeyPair
                initialize(ecSpec, random)
                generateKeyPair().toEncoded()
            }
    }

}

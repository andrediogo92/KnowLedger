package org.knowledger.ledger.crypto

import kotlinx.serialization.Serializable

@Serializable
data class EncodedKeyPair(val publicKey: EncodedPublicKey, val privateKey: EncodedPrivateKey)
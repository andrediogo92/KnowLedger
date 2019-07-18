package org.knowledger.ledger.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.knowledger.ledger.core.misc.getStringFromKey
import org.knowledger.ledger.core.misc.stringToPublicKey
import java.security.PublicKey

class PublicKeyJsonAdapter {
    @ToJson
    fun publicKeyToJson(publicKey: PublicKey): String =
        publicKey.getStringFromKey()

    @FromJson
    fun publicKeyFromJson(publicKey: String): PublicKey =
        publicKey.stringToPublicKey()
}
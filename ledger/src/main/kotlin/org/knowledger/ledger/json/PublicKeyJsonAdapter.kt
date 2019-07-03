package org.knowledger.ledger.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.knowledger.common.misc.getStringFromKey
import org.knowledger.common.misc.stringToPublicKey
import java.security.PublicKey

class PublicKeyJsonAdapter {
    @ToJson
    fun publicKeyToJson(publicKey: PublicKey): String =
        publicKey.getStringFromKey()

    @FromJson
    fun publicKeyFromJson(publicKey: String): PublicKey =
        publicKey.stringToPublicKey()
}
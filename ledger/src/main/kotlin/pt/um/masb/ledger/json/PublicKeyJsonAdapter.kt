package pt.um.masb.ledger.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import pt.um.masb.common.misc.getStringFromKey
import pt.um.masb.common.misc.stringToPublicKey
import java.security.PublicKey

class PublicKeyJsonAdapter {
    @ToJson
    fun publicKeyToJson(publicKey: PublicKey): String =
        publicKey.getStringFromKey()

    @FromJson
    fun publicKeyFromJson(publicKey: String): PublicKey =
        stringToPublicKey(publicKey)
}
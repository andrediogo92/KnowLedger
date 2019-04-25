package pt.um.lei.masb.blockchain.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import pt.um.lei.masb.blockchain.utils.getStringFromKey
import pt.um.lei.masb.blockchain.utils.stringToPublicKey
import java.security.PublicKey

class PublicKeyJsonAdapter {
    @ToJson
    fun publicKeyToJson(publicKey: PublicKey): String =
        getStringFromKey(publicKey)

    @FromJson
    fun publicKeyFromJson(publicKey: String): PublicKey =
        stringToPublicKey(publicKey)
}
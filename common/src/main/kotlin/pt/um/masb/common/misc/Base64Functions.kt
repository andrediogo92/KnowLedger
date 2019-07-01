package pt.um.masb.common.misc

import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import java.security.Key
import java.util.*

private val b64Encoder = Base64.getUrlEncoder()
private val b64Decoder = Base64.getUrlDecoder()


fun base64Encode(
    toEncode: Hash
): String =
    base64Encode(toEncode.bytes)


@UseExperimental(ExperimentalStdlibApi::class)
fun base64Encode(
    toEncode: String
): String =
    base64Encode(toEncode.encodeToByteArray())

fun base64Encode(
    toEncode: ByteArray
): String =
    b64Encoder.encodeToString(toEncode)

fun base64Decode(
    toDecode: String
): ByteArray =
    b64Decoder.decode(toDecode)

fun base64DecodeToHash(
    toDecode: String
): Hash =
    Hash(base64Decode(toDecode))

@UseExperimental(ExperimentalStdlibApi::class)
fun base64DecodeToString(
    toDecode: String
): String =
    base64Decode(toDecode).decodeToString()

fun getStringFromKey(
    key: Key
): String =
    base64Encode(key.encoded)

fun <T> extractIdFromClass(clazz: Class<T>): String =
    base64Encode(
        AvailableHashAlgorithms.SHA256Hasher.applyHash(
            clazz.toGenericString()
        )
    )
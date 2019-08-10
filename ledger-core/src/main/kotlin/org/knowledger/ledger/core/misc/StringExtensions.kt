package org.knowledger.ledger.core.misc

import org.knowledger.ledger.core.hash.AvailableHashAlgorithms

@Suppress("NOTHING_TO_INLINE")
@UseExperimental(ExperimentalStdlibApi::class)
inline fun String.encodeStringToUTF8(): ByteArray =
    this.encodeToByteArray()

@Suppress("NOTHING_TO_INLINE")
@UseExperimental(ExperimentalStdlibApi::class)
inline fun ByteArray.decodeUTF8ToString(): String =
    this.decodeToString()

val <T> Class<T>.classDigest: String
    get() = AvailableHashAlgorithms.SHA3256Hasher.applyHash(
        toGenericString()
    ).base64Encode()
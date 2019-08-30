package org.knowledger.ledger.core.misc

@Suppress("NOTHING_TO_INLINE")
@UseExperimental(ExperimentalStdlibApi::class)
inline fun String.encodedStringInUTF8(): ByteArray = encodeToByteArray()

@Suppress("NOTHING_TO_INLINE")
@UseExperimental(ExperimentalStdlibApi::class)
inline fun ByteArray.decodedUTF8String(): String = decodeToString()
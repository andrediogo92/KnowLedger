package pt.um.masb.common.misc

import pt.um.masb.common.config.LedgerConfiguration
import java.security.Key

@Suppress("NOTHING_TO_INLINE")
@UseExperimental(ExperimentalStdlibApi::class)
inline fun String.encodeStringToUTF8(): ByteArray =
    this.encodeToByteArray()

@Suppress("NOTHING_TO_INLINE")
@UseExperimental(ExperimentalStdlibApi::class)
inline fun ByteArray.decodeUTF8ToString(): String =
    this.decodeToString()

fun Key.getStringFromKey(): String =
    base64Encode(encoded)

fun <T> Class<T>.extractIdFromClass(): String =
    base64Encode(
        LedgerConfiguration.DEFAULT_CRYPTER.applyHash(
            this.toGenericString()
        )
    )
package pt.um.lei.masb.blockchain.utils

import kotlin.experimental.and

fun ByteArray.asHex(): String =
// Only convert on print.
    StringBuilder().let {
        for (bHash in this) {
            val hex = String.format(
                "%02X",
                bHash
            )
            it.append(hex)
        }
        it.toString()
    }


fun Long.bytes(): ByteArray {
    val magic = Long.SIZE_BYTES / Byte.SIZE_BYTES
    val result = ByteArray(magic)
    var l = this
    for (i in magic - 1 downTo 0) {
        result[i] = (l.toByte() and 0xFF.toByte())
        l = l shr magic
    }
    return result
}

fun Int.bytes(): ByteArray {
    val magic = Int.SIZE_BYTES / Byte.SIZE_BYTES
    val result = ByteArray(magic)
    var l = this
    for (i in magic - 1 downTo 0) {
        result[i] = (l.toByte() and 0xFF.toByte())
        l = l shr magic
    }
    return result
}

fun Double.bytes(): ByteArray {
    val magic = Long.SIZE_BYTES / Byte.SIZE_BYTES
    val result = ByteArray(magic)
    var l = this.toRawBits()
    for (i in magic - 1 downTo 0) {
        result[i] = (l.toByte() and 0xFF.toByte())
        l = l shr magic
    }
    return result
}

fun flattenBytes(
    byteArrays: Array<ByteArray>,
    vararg bytes: Byte
): ByteArray {
    val final = ByteArray(
        byteArrays.sumBy { it.size } + bytes.size
    )
    var into = 0
    byteArrays.forEach {
        it.copyInto(final, into)
        into += it.size
    }
    bytes.copyInto(final, into)
    return final
}

fun flattenBytes(vararg byteArrays: ByteArray): ByteArray {
    val final = ByteArray(byteArrays.sumBy { it.size })
    var into = 0
    byteArrays.forEach {
        it.copyInto(final, into)
        into += it.size
    }
    return final
}

fun flattenBytes(
    collection: Collection<ByteArray>,
    vararg bytesArrays: ByteArray
): ByteArray {
    val final = ByteArray(
        collection.sumBy { it.size } + bytesArrays.sumBy { it.size }
    )
    var into = 0
    collection.forEach {
        it.copyInto(final, into)
        into += it.size
    }
    bytesArrays.forEach {
        it.copyInto(final, into)
        into += it.size
    }
    return final
}
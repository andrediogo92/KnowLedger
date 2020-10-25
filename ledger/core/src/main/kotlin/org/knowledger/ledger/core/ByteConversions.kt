package org.knowledger.ledger.core

import java.math.BigDecimal
import java.time.Instant
import kotlin.experimental.and

@Suppress("DuplicatedCode")
val Long.bytes: ByteArray
    get() {
        val sizeOfByte = Byte.SIZE_BITS
        val sizeInBytes = Long.SIZE_BYTES / Byte.SIZE_BYTES
        val result = ByteArray(sizeInBytes)
        var l = this
        for (i in sizeInBytes - 1 downTo 0) {
            result[i] = (l.toByte() and 0xFF.toByte())
            l = l shr sizeOfByte
        }
        return result
    }

@Suppress("DuplicatedCode")
val Int.bytes: ByteArray
    get() {
        val sizeOfByte = Byte.SIZE_BITS
        val sizeInBytes = Int.SIZE_BYTES / Byte.SIZE_BYTES
        val result = ByteArray(sizeInBytes)
        var l = this
        for (i in sizeInBytes - 1 downTo 0) {
            result[i] = (l.toByte() and 0xFF.toByte())
            l = l shr sizeOfByte
        }
        return result
    }

val Double.bytes: ByteArray get() = toRawBits().bytes

/**
 * Byte concatenation of epoch seconds and leftover nanos.
 */
val Instant.bytes: ByteArray get() = epochSecond.bytes + nano.bytes

/**
 * Byte concatenation of [UUID] via mostSignificantBits +
 * leastSignificantBits (big-endian order).
 */
val UUID.bytes: ByteArray get() = mostSignificantBits.bytes + leastSignificantBits.bytes

/**
 * Extracts bytes from [BigDecimal.unscaledValue] directly
 * converted to [ByteArray].
 */
val BigDecimal.bytes: ByteArray get() = unscaledValue().toByteArray()


fun flattenBytes(byteArrays: Array<ByteArray>, vararg bytes: Byte): ByteArray {
    val final = ByteArray(byteArrays.sumBy { it.size } + bytes.size)
    var into = 0
    byteArrays.forEach {
        it.copyInto(final, into)
        into += it.size
    }
    bytes.copyInto(final, into)
    return final
}

fun flattenBytes(vararg byteArrays: ByteArray): ByteArray =
    flattenCollectionsAndVarargs(
        ByteArray(byteArrays.sumBy { it.size }), null, byteArrays
    )

fun flattenBytes(collection: Collection<ByteArray>, vararg byteArrays: ByteArray): ByteArray =
    flattenCollectionsAndVarargs(
        ByteArray(collection.sumBy { it.size } + byteArrays.sumBy { it.size }),
        collection.iterator(), byteArrays
    )

fun flattenBytes(collection: Iterable<ByteArray>, vararg byteArrays: ByteArray): ByteArray =
    flattenCollectionsAndVarargs(
        ByteArray(collection.sumBy { it.size } + byteArrays.sumBy { it.size }),
        collection.iterator(), byteArrays
    )


fun flattenBytes(
    collectionSize: Int, collection: Sequence<ByteArray>, vararg byteArrays: ByteArray,
): ByteArray = flattenCollectionsAndVarargs(
    ByteArray(collectionSize + byteArrays.sumBy { it.size }),
    collection.iterator(), byteArrays
)

private fun flattenCollectionsAndVarargs(
    final: ByteArray, collection: Iterator<ByteArray>?, byteArrays: Array<out ByteArray>,
): ByteArray {
    var into = 0
    collection?.forEach {
        it.copyInto(final, into)
        into += it.size
    }
    byteArrays.forEach {
        it.copyInto(final, into)
        into += it.size
    }
    return final

}

package org.knowledger.ledger.core.misc

import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import kotlin.experimental.and

@Suppress("DuplicatedCode", "SameParameterValue")
private fun loopShifts(
    sizeInBytes: Int, sizeOfByte: Int,
    accum: Int
): ByteArray {
    val result = ByteArray(sizeInBytes)
    var l = accum
    for (i in sizeInBytes - 1 downTo 0) {
        result[i] = (l.toByte() and 0xFF.toByte())
        l = l shr sizeOfByte
    }
    return result
}

@Suppress("DuplicatedCode", "SameParameterValue")
private fun loopShifts(
    sizeInBytes: Int, sizeOfByte: Int,
    accum: Long
): ByteArray {
    val result = ByteArray(sizeInBytes)
    var l = accum
    for (i in sizeInBytes - 1 downTo 0) {
        result[i] = (l.toByte() and 0xFF.toByte())
        l = l shr sizeOfByte
    }
    return result
}


fun Long.toBytes(): ByteArray = loopShifts(
    Long.SIZE_BYTES / Byte.SIZE_BYTES,
    Byte.SIZE_BITS, this
)

fun Int.toBytes(): ByteArray = loopShifts(
    Int.SIZE_BYTES / Byte.SIZE_BYTES,
    Byte.SIZE_BITS, this
)

fun Double.toBytes(): ByteArray = loopShifts(
    Long.SIZE_BYTES / Byte.SIZE_BYTES,
    Byte.SIZE_BITS, toRawBits()
)

/**
 * Byte concatenation of epoch seconds and leftover nanos.
 */
fun Instant.toBytes(): ByteArray =
    epochSecond.toBytes() + nano.toBytes()

/**
 * Byte concatenation of [UUID] via [UUID.getMostSignificantBits] +
 * [UUID.getLeastSignificantBits] (big-endian order).
 */
fun UUID.toBytes(): ByteArray =
    mostSignificantBits.toBytes() + leastSignificantBits.toBytes()

/**
 * Extracts bytes from [BigDecimal.unscaledValue] directly
 * converted to [ByteArray].
 */
fun BigDecimal.toBytes(): ByteArray =
    unscaledValue().toByteArray()

fun Payout.toBytes(): ByteArray =
    payout.unscaledValue().toByteArray()

fun Difficulty.toBytes(): ByteArray =
    difficulty.toByteArray()


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

fun flattenBytes(
    vararg byteArrays: ByteArray
): ByteArray =
    flattenCollectionsAndVarargs(
        ByteArray(byteArrays.sumBy { it.size }),
        null, byteArrays
    )

fun flattenBytes(
    collection: Collection<ByteArray>,
    vararg byteArrays: ByteArray
): ByteArray =
    flattenCollectionsAndVarargs(
        ByteArray(
            collection.sumBy { it.size } + byteArrays.sumBy { it.size }
        ), collection.iterator(), byteArrays
    )

fun flattenBytes(
    collection: Iterable<ByteArray>,
    vararg byteArrays: ByteArray
): ByteArray =
    flattenCollectionsAndVarargs(
        ByteArray(
            collection.sumBy { it.size } + byteArrays.sumBy { it.size }
        ), collection.iterator(), byteArrays
    )


fun flattenBytes(
    collectionSize: Int,
    collection: Sequence<ByteArray>,
    vararg byteArrays: ByteArray
): ByteArray =
    flattenCollectionsAndVarargs(
        ByteArray(
            collectionSize + byteArrays.sumBy { it.size }
        ), collection.iterator(), byteArrays
    )

private fun flattenCollectionsAndVarargs(
    final: ByteArray,
    collection: Iterator<ByteArray>?,
    byteArrays: Array<out ByteArray>
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

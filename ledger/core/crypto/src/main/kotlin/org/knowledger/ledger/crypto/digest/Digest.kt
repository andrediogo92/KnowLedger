package org.knowledger.ledger.crypto.digest

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashers
import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.KClass

internal val nameComparator = Comparator<KClass<*>> { o1, o2 ->
    o1.simpleName!!.compareTo(o2.simpleName!!)
}

internal val hardCodedClazzes = arrayOf<KClass<*>>(
    BigDecimal::class, BigInteger::class, Class::class, Hash::class,
    KClass::class, Lazy::class, MessageDigest::class, String::class
)

private val hardCodedInterning = arrayOf(
    InterningEnum.BIGDECIMAL, InterningEnum.BIGINTEGER, InterningEnum.CLASS,
    InterningEnum.HASH, InterningEnum.KCLASS, InterningEnum.LAZY,
    InterningEnum.MESSAGEDIGEST, InterningEnum.STRING
)

internal val hardCodedHashes: Array<(Int) -> Hash> = arrayOf(
    ::digestBigDecimal, ::digestBigInteger, ::digestClass, ::digestHash,
    ::digestKClass, ::digestLazy, ::digestMessageDigest, ::digestString,
    ::digestCycle
)

internal val arrayClazzes = arrayOf<KClass<*>>(
    BooleanArray::class, ByteArray::class, CharArray::class, DoubleArray::class,
    FloatArray::class, IntArray::class, LongArray::class, ShortArray::class,
)

private val arrayInterning = arrayOf(
    InterningEnum.BOOLEANARRAY, InterningEnum.BYTEARRAY, InterningEnum.CHARARRAY,
    InterningEnum.DOUBLEARRAY, InterningEnum.FLOATARRAY, InterningEnum.INTARRAY,
    InterningEnum.LONGARRAY, InterningEnum.SHORTARRAY
)

internal val primitiveClazzes = arrayOf<KClass<*>>(
    Boolean::class, Byte::class, Char::class, Double::class,
    Float::class, Int::class, Long::class, Short::class,
//          UByte::class, UShort::class, UInt::class, ULong::class,
)

private val primitiveInterning = arrayOf(
    InterningEnum.BOOLEAN, InterningEnum.BYTE, InterningEnum.CHAR,
    InterningEnum.DOUBLE, InterningEnum.FLOAT, InterningEnum.INT,
    InterningEnum.LONG, InterningEnum.SHORT
)

private fun fromClass(
    clazz: KClass<*>, array: Array<KClass<*>>, interning: Array<InterningEnum>,
): InterningEnum {
    val index = array.binarySearch(clazz, nameComparator)
    return interning[index]
}

internal fun internFromHardcoded(clazz: KClass<*>): InterningEnum =
    fromClass(clazz, hardCodedClazzes, hardCodedInterning)

internal fun internFromHardcoded(index: Int): InterningEnum =
    hardCodedInterning[index]

internal fun internFromArray(clazz: KClass<*>): InterningEnum =
    fromClass(clazz, arrayClazzes, arrayInterning)

internal fun internFromArray(index: Int): InterningEnum =
    arrayInterning[index]

internal fun internFromPrimitive(clazz: KClass<*>): InterningEnum =
    fromClass(clazz, primitiveClazzes, primitiveInterning)

internal fun internFromPrimitive(index: Int): InterningEnum =
    primitiveInterning[index]

private fun digestBigDecimal(size: Int): Hash =
    Hash(ByteArray(size).also { it[size - 1] = 0x7F })

private fun digestBigInteger(size: Int): Hash =
    Hash(ByteArray(size).also { it[size - 1] = -0x7F })

private fun digestClass(size: Int): Hash = Hash(ByteArray(size) { 0x1F })

private fun digestHash(size: Int): Hash =
    Hash(ByteArray(size).also { it[size - 1] = 0x1D; it[size - 2] = 0x3E })

private fun digestKClass(size: Int): Hash = Hash(ByteArray(size) { 0x2F })

private fun digestLazy(size: Int): Hash = Hash(ByteArray(size) { 0x4E })

private fun digestMessageDigest(size: Int): Hash =
    Hash(ByteArray(size).also { it[0] = -0x80; it[1] = 0x44 })

private fun digestString(size: Int): Hash =
    Hash(ByteArray(size).also { it[size - 1] = -0x80; it[size - 2] = -0x80 })

private fun digestCycle(size: Int): Hash = Hash(ByteArray(size) { 0x7F })


fun <T : Any> T.classDigest(hashers: Hashers): SchemaHash =
    this::class.classDigest(hashers)


fun <T : Any> KClass<in T>.classDigest(hashers: Hashers): Hash =
    if (isFun) {
        throw ClassCastException("Can't resolve fields of functional interface type:$qualifiedName")
    } else {
        DigestState(hashers).calculateHash(this)
    }


inline fun <reified T : Any> classDigest(hashers: Hashers): Hash =
    T::class.classDigest(hashers)
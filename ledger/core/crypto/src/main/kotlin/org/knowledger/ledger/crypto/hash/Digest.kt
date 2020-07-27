package org.knowledger.ledger.crypto.hash

import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.data.hash.Hash
import org.knowledger.ledger.crypto.Hashers
import org.tinylog.kotlin.Logger
import java.lang.reflect.ParameterizedType
import java.math.BigDecimal
import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import kotlin.collections.ArrayDeque

private data class DigestState(
    var field: ByteArray, var clazz: Class<*>,
    var state: State, var type: ByteArray = byteArrayOf(),
    var params: Array<Class<*>>? = null
) {
    enum class State {
        FirstPass,
        Composite,
        Cycle,
        Processed
    }

    internal fun enumDigest(): DigestState =
        apply {
            type = "${clazz.toGenericString()}${clazz.enumConstants.joinToString()}".toByteArray()
            state = State.Processed
        }

    internal fun primitiveDigest(): DigestState =
        apply {
            type = clazz.canonicalName.toByteArray()
            state = State.Processed
        }

    internal fun checkMap(): Boolean =
        if (map.containsKey(clazz)) {
            type = map.getValue(clazz)()
            state = State.Processed
            true
        } else {
            false
        }

    internal fun applyDigest(hashers: Hashers): Tag =
        hashers.applyHash(type)

    internal fun markArray() {
        state = State.Composite
        //"[]" in UTF-8
        type = byteArrayOf(0x5b, 0x5d)
    }

    internal fun markExpand() {
        state = State.Composite
        type = byteArrayOf()
    }

    internal fun markCycle() {
        state = State.Cycle
        type = digestCycle()
    }


    internal fun compact(tag: Tag) {
        type = tag.bytes
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DigestState

        if (!field.contentEquals(other.field)) return false
        if (clazz != other.clazz) return false
        if (state != other.state) return false
        if (!type.contentEquals(other.type)) return false
        if (params != null) {
            if (other.params == null) return false
            if (!params!!.contentEquals(other.params!!)) return false
        } else if (other.params != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = field.hashCode()
        result = 31 * result + clazz.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (params?.contentHashCode() ?: 0)
        return result
    }

    companion object {
        val map: Map<Class<*>, () -> ByteArray> = mapOf(
            BigDecimal::class.java to Companion::digestBigDecimal,
            BigInteger::class.java to Companion::digestBigInteger,
            String::class.java to Companion::digestString,
            UUID::class.java to Companion::digestUUID,
            PrivateKey::class.java to Companion::digestPrivateKey,
            PublicKey::class.java to Companion::digestPublicKey,
            Hash::class.java to Companion::digestHash
        )

        private fun digestCycle(): ByteArray =
            ByteArray(32)

        private fun digestBigDecimal(): ByteArray =
            ByteArray(32).also {
                it[31] = 0x7F
            }

        private fun digestBigInteger(): ByteArray =
            ByteArray(32).also {
                it[31] = -0x7F
            }

        private fun digestUUID(): ByteArray =
            ByteArray(32).also {
                it[31] = 0x7F
                it[30] = 0x7F
            }

        private fun digestString(): ByteArray =
            ByteArray(32).also {
                it[31] = -0x80
                it[30] = -0x80
            }


        private fun digestPrivateKey(): ByteArray =
            ByteArray(32).also {
                it[31] = 0x2C
                it[30] = 0x2C
            }

        private fun digestPublicKey(): ByteArray =
            ByteArray(32).also {
                it[31] = -0x2C
                it[30] = -0x2C
            }

        private fun digestHash(): ByteArray =
            ByteArray(32).also {
                it[31] = 0x1D
                it[30] = 0x3E
            }
    }
}


fun <T : Any> T.classDigest(hashers: Hashers): Tag =
    javaClass.classDigest(hashers)


@OptIn(ExperimentalStdlibApi::class)
fun <T : Any> Class<in T>.classDigest(hashers: Hashers): Tag {
    if (isInterface) {
        throw ClassCastException("Can't resolve fields of interface type: $canonicalName")
    }
    val states: ArrayDeque<DigestState> = ArrayDeque()
    states.addLast(DigestState(byteArrayOf(), this, DigestState.State.FirstPass))
    //Fill up states by breadth first search until all leaves have been resolved.
    while (states[0].state == DigestState.State.FirstPass) {
        levelPass(states, states.takeWhile { it.state == DigestState.State.FirstPass })
    }
    val feed = ArrayDeque<DigestState>()
    while (states.size > 1) {
        while (states[0].state != DigestState.State.Composite) {
            feed.addLast(states.removeFirst())
        }

        compactLevel(hashers, states, feed)
        feed.clear()
    }
    return hashers.applyHash(states.removeFirst().type)
}

@ExperimentalStdlibApi
private fun compactLevel(
    hashers: Hashers,
    feedback: ArrayDeque<DigestState>,
    states: ArrayDeque<DigestState>
) {
    val builder = StringBuilder()
    states.forEach {
        builder.append(it.field).append(it.type)
    }
    feedback[0].compact(hashers.applyHash(builder.toString()))
}

/**
 * Level pass does one level of a breadth first search, resolving final types or
 * expanding into their fields for later compacting.
 */
@ExperimentalStdlibApi
private fun levelPass(
    feedback: ArrayDeque<DigestState>,
    states: List<DigestState>
) {
    for (digestState in states) {
        Logger.debug { "Level Pass :> Current: ${digestState.clazz.canonicalName}" }
        when {
            digestState.clazz.isArray -> {
                digestState.markArray()
                feedback.addFirst(
                    DigestState(
                        byteArrayOf(), digestState.clazz.componentType,
                        DigestState.State.FirstPass
                    )
                )
            }
            digestState.clazz.isEnum -> {
                digestState.enumDigest()
            }
            digestState.clazz.isPrimitive -> {
                digestState.primitiveDigest()
            }
            digestState.checkMap() -> {
            }
            else -> {
                expandFields(feedback, digestState)
            }
        }
    }
}

@ExperimentalStdlibApi
private fun expandFields(
    states: ArrayDeque<DigestState>,
    digestState: DigestState
) {
    if (digestState.clazz.declaredFields.isEmpty()) {
        digestState.primitiveDigest()
    } else {
        digestState.markExpand()
        digestState.clazz.declaredFields
            .filter { it.name != "Companion" }
            .forEach { field ->
                if (field.type == digestState.clazz) {
                    states.addFirst(
                        DigestState(
                            field.name.toByteArray(), digestState.clazz, DigestState.State.Processed
                        ).also { it.markCycle() }
                    )
                } else {
                    when (field.genericType) {
                        is ParameterizedType -> states.addFirst(
                            DigestState(
                                field.name.toByteArray(), field.type,
                                DigestState.State.FirstPass, byteArrayOf(),
                                (field.genericType as ParameterizedType).actualTypeArguments
                                    .map { it as Class<*> }
                                    .toTypedArray()
                            )
                        )
                        else -> states.addFirst(
                            DigestState(field.name.toByteArray(), field.type, DigestState.State.FirstPass)
                        )
                    }

                }
            }
    }
}

inline fun <reified T : Any> classDigest(hashers: Hashers): Tag =
    T::class.java.classDigest(hashers)
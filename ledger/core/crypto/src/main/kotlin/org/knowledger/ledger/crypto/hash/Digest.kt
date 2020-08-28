package org.knowledger.ledger.crypto.hash

import org.knowledger.collections.forEach
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashers
import org.tinylog.kotlin.Logger
import java.lang.reflect.ParameterizedType
import java.math.BigDecimal
import java.math.BigInteger

private data class DigestState(
    var field: ByteArray, var clazz: Class<*>,
    var state: State, var type: ByteArray = byteArrayOf(),
    var params: Array<Class<*>>? = null,
) {
    enum class State {
        FirstPass,
        Composite,
        Cycle,
        Processed
    }

    fun enumDigest(): DigestState = apply {
        type = "${clazz.toGenericString()}${clazz.enumConstants.joinToString()}".toByteArray()
        state = State.Processed
    }

    fun primitiveDigest(): DigestState = apply {
        type = clazz.canonicalName.toByteArray()
        state = State.Processed
    }

    fun checkMap(): Boolean = map.containsKey(clazz)

    fun getFromHardcodedMap(hashSize: Int): DigestState = apply {
        type = map.getValue(clazz)(hashSize)
        state = State.Processed
    }


    fun markArray() {
        state = State.Composite
        type = byteArrayOf()
    }

    fun markExpand() {
        state = State.Composite
        type = byteArrayOf()
    }

    fun markCompacted() {
        state = State.Processed
    }


    fun compact(tag: Hash) {
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
        val map: Map<Class<*>, (Int) -> ByteArray> = mapOf(
            BigDecimal::class.java to Companion::digestBigDecimal,
            BigInteger::class.java to Companion::digestBigInteger,
            String::class.java to Companion::digestString,
            Hash::class.java to Companion::digestHash
        )

        internal fun digestCycle(size: Int): ByteArray = ByteArray(size) { 0x7F }

        private fun digestBigDecimal(size: Int): ByteArray = ByteArray(size).also {
            it[size - 1] = 0x7F
        }

        private fun digestBigInteger(size: Int): ByteArray = ByteArray(size).also {
            it[size - 1] = -0x7F
        }

        private fun digestString(size: Int): ByteArray = ByteArray(size).also {
            it[size - 1] = -0x80; it[size - 2] = -0x80
        }

        private fun digestHash(size: Int): ByteArray = ByteArray(size).also {
            it[size - 1] = 0x1D; it[size - 2] = 0x3E
        }
    }
}


fun <T : Any> T.classDigest(hashers: Hashers): Hash =
    javaClass.classDigest(hashers)


fun <T : Any> Class<in T>.classDigest(hashers: Hashers): Hash {
    if (isInterface) {
        throw ClassCastException("Can't resolve fields of interface type: $canonicalName")
    }
    val states: ArrayDeque<DigestState> = ArrayDeque()
    states.addLast(DigestState(byteArrayOf(), this, DigestState.State.FirstPass))
    //Fill up states by breadth first search until all leaves have been resolved.
    while (states[0].state == DigestState.State.FirstPass) {
        val filteredStates = states.takeWhile { it.state == DigestState.State.FirstPass }
        levelPass(states, filteredStates, hashers.hashSize)
    }
    val temporaryStates = ArrayDeque<DigestState>()
    while (states.size > 1) {
        while (states[0].state != DigestState.State.Composite) {
            temporaryStates.addLast(states.removeFirst())
        }

        compactLevel(hashers, temporaryStates, states)
        temporaryStates.clear()
    }
    return hashers.applyHash(states.removeFirst().type)
}

private fun compactLevel(
    hashers: Hashers, filteredStates: ArrayDeque<DigestState>, states: ArrayDeque<DigestState>,
) {
    val builder = StringBuilder()
    filteredStates.forEach { builder.append(it.field).append(it.type) }
    with(states[0]) {
        compact(hashers.applyHash(builder.toString()))
        markCompacted()
    }
}

/**
 * Level pass does one level of a breadth first search, resolving final types or
 * expanding into their fields for later compacting.
 */
private fun levelPass(feedback: ArrayDeque<DigestState>, states: List<DigestState>, hashSize: Int) {
    states.forEach { digestState ->
        Logger.debug { "Level Pass :> Current: ${digestState.clazz.canonicalName}" }
        when {
            digestState.clazz.isArray -> {
                digestState.markArray()
                val component = digestState.clazz.componentType
                feedback.addFirst(
                    DigestState(
                        "[${component.name}".toByteArray(), component,
                        DigestState.State.FirstPass
                    )
                )
            }
            digestState.clazz.isEnum -> digestState.enumDigest()
            digestState.clazz.isPrimitive -> digestState.primitiveDigest()
            digestState.checkMap() -> digestState.getFromHardcodedMap(hashSize)
            else -> expandFields(feedback, digestState, hashSize)
        }
    }
}

private fun expandFields(states: ArrayDeque<DigestState>, digestState: DigestState, hashSize: Int) {
    if (digestState.clazz.declaredFields.isEmpty()) {
        digestState.primitiveDigest()
        return
    }

    digestState.markExpand()
    digestState.clazz.declaredFields.asSequence()
        .filter { it.name != "Companion" }
        .partition { it.type != digestState.clazz }
        .forEach(
            { field ->
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
                        DigestState(
                            field.name.toByteArray(), field.type, DigestState.State.FirstPass
                        )
                    )
                }

            }, { cycle ->
                states.addFirst(
                    DigestState(
                        cycle.name.toByteArray(), cycle.type,
                        DigestState.State.Cycle, DigestState.digestCycle(hashSize)
                    )
                )
            })
}

inline fun <reified T : Any> classDigest(hashers: Hashers): Hash =
    T::class.java.classDigest(hashers)
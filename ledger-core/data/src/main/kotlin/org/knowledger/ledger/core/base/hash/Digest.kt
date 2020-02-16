package org.knowledger.ledger.core.base.hash

import org.knowledger.ledger.core.base.data.Tag
import org.knowledger.ledger.core.base.data.toHexString
import org.tinylog.kotlin.Logger
import java.lang.reflect.ParameterizedType
import java.math.BigDecimal
import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*


private data class DigestState(
    var field: String, var clazz: Class<*>,
    var state: State, var type: String = "",
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
            type = "${clazz.toGenericString()}${clazz.enumConstants.joinToString()}"
            state = State.Processed
        }

    internal fun primitiveDigest(): DigestState =
        apply {
            type = clazz.canonicalName
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

    internal fun applyDigest(hasher: Hasher): Tag =
        hasher.applyHash(type)

    internal fun markArray() {
        state = State.Composite
        type = "[]"
    }

    internal fun markExpand() {
        state = State.Composite
        type = ""
    }

    internal fun markCycle() {
        state = State.Cycle
        type = digestCycle()
    }


    internal fun compact(tag: Tag) {
        type = tag.toHexString()
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DigestState

        if (field != other.field) return false
        if (clazz != other.clazz) return false
        if (state != other.state) return false
        if (type != other.type) return false
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
        val map: Map<Class<*>, () -> String> = mapOf(
            BigDecimal::class.java to Companion::digestBigDecimal,
            BigInteger::class.java to Companion::digestBigInteger,
            String::class.java to Companion::digestString,
            UUID::class.java to Companion::digestUUID,
            PrivateKey::class.java to Companion::digestPrivateKey,
            PublicKey::class.java to Companion::digestPublicKey,
            Hash::class.java to Companion::digestHash
        )

        private fun digestCycle(): String =
            "8340e244-8735-451d-bfea-40d0599dba22"

        private fun digestBigDecimal(): String =
            "290d85d2-d360-443e-8269-b75d04a6ee87"

        private fun digestBigInteger(): String =
            "fefe9f33-8423-4597-b4ff-7820a177da2c"

        private fun digestUUID(): String =
            "6de7f88d-f334-48c5-9252-7d256febc1e1"

        private fun digestString(): String =
            "d2777356-9e71-44a5-bb68-20e345eb83cb"


        private fun digestPrivateKey(): String =
            "f39c7e6a-6e0a-4c13-b9d1-8c7434820559"

        private fun digestPublicKey(): String =
            "5e2fc0c1-7ae8-438a-81d1-16203f3e2be7"

        private fun digestHash(): String =
            "bc32c035-d7be-44ad-b2f9-95fa945e9e55"
    }
}


fun <T : Any> T.classDigest(hasher: Hasher): Tag =
    javaClass.classDigest(hasher)


fun <T : Any> Class<in T>.classDigest(hasher: Hasher): Tag {
    if (isInterface) {
        throw ClassCastException("Can't resolve fields of interface type: $canonicalName")
    }
    val states: ArrayDeque<DigestState> = ArrayDeque()
    states.offer(DigestState("", this, DigestState.State.FirstPass))
    //Fill up states by breadth first search until all leaves have been resolved.
    while (states.first.state == DigestState.State.FirstPass) {
        levelPass(states, states.takeWhile { it.state == DigestState.State.FirstPass })
    }
    val feed = ArrayDeque<DigestState>()
    while (states.size > 1) {
        while (states.peekFirst().state != DigestState.State.Composite) {
            val item = states.pollFirst()
            feed.offerLast(item)
        }

        compactLevel(hasher, states, feed)
        feed.clear()
    }
    return hasher.applyHash(states.pollFirst().type)
}

private fun compactLevel(
    hasher: Hasher,
    feedback: ArrayDeque<DigestState>,
    states: ArrayDeque<DigestState>
) {
    val type = StringBuilder()
    states.forEach {
        type.append(it.field)
        type.append(it.type)
    }
    feedback
        .peekFirst()
        .compact(
            hasher.applyHash(type.toString())
        )
}

/**
 * Level pass does one level of a breadth first search, resolving final types or
 * expanding into their fields for later compacting.
 */
private fun levelPass(
    feedback: ArrayDeque<DigestState>,
    states: List<DigestState>
) {
    for (digestState in states) {
        Logger.debug { "Level Pass :> Current: ${digestState.clazz.canonicalName}" }
        when {
            digestState.clazz.isArray -> {
                digestState.markArray()
                feedback.offerFirst(
                    DigestState(
                        clazz = digestState.clazz.componentType,
                        field = "",
                        state = DigestState.State.FirstPass
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

private fun expandFields(
    states: ArrayDeque<DigestState>,
    digestState: DigestState
) {
    if (digestState.clazz.declaredFields.isEmpty()) {
        digestState.primitiveDigest()
    } else {
        digestState.markExpand()
        digestState
            .clazz
            .declaredFields
            .filter { it.name != "Companion" }
            .forEach { field ->
                if (field.type == digestState.clazz) {
                    states.offerFirst(
                        DigestState(
                            field = field.name,
                            clazz = digestState.clazz,
                            state = DigestState.State.Processed
                        ).also { it.markCycle() }
                    )
                } else {
                    when (field.genericType) {
                        is ParameterizedType -> {
                            states.offerFirst(
                                DigestState(
                                    field = field.name,
                                    clazz = field.type,
                                    params = (field.genericType as ParameterizedType)
                                        .actualTypeArguments
                                        .map { it as Class<*> }
                                        .toTypedArray(),
                                    state = DigestState.State.FirstPass
                                )
                            )
                        }
                        else -> {
                            states.offerFirst(
                                DigestState(
                                    field = field.name,
                                    clazz = field.type,
                                    state = DigestState.State.FirstPass
                                )
                            )
                        }
                    }

                }
            }
    }
}

inline fun <reified T : Any> classDigest(hasher: Hasher): Tag =
    T::class.java.classDigest(hasher)
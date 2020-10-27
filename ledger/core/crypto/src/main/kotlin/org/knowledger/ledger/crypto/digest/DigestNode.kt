package org.knowledger.ledger.crypto.digest

import org.knowledger.ledger.crypto.Hash
import kotlin.reflect.KClassifier

internal data class DigestNode(
    var field: ByteArray, var classifier: KClassifier,
    var state: State = State.FirstPass,
    var typeHash: Hash = Hash.emptyHash,
    var params: Array<KClassifier>? = null,
) {
    lateinit var parent: DigestNode

    enum class State {
        FirstPass,
        Composite,
        Cycle,
        Processed
    }

    fun markExpand() {
        state = State.Composite
        typeHash = Hash.emptyHash
    }

    fun compact(tag: Hash) {
        typeHash = tag
        state = State.Processed
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DigestNode

        if (!field.contentEquals(other.field)) return false
        if (classifier != other.classifier) return false
        if (state != other.state) return false
        if (typeHash != other.typeHash) return false
        if (params != null) {
            if (other.params == null) return false
            if (!params.contentEquals(other.params)) return false
        } else if (other.params != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = field.contentHashCode()
        result = 31 * result + classifier.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + typeHash.hashCode()
        result = 31 * result + (params?.contentHashCode() ?: 0)
        return result
    }
}
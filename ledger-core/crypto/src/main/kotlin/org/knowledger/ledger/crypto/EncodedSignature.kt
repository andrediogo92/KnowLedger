package org.knowledger.ledger.crypto

data class EncodedSignature(val encoded: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncodedSignature) return false

        if (!encoded.contentEquals(other.encoded)) return false

        return true
    }

    override fun hashCode(): Int {
        return encoded.contentHashCode()
    }
}
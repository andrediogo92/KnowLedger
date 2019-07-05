package org.knowledger.ledger.config

import com.squareup.moshi.JsonClass
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.encodeStringToUTF8

@JsonClass(generateAdapter = true)
class StorageUnawareChainId internal constructor(
    override val tag: String,
    override val ledgerHash: Hash,
    override val hashId: Hash
) : ChainId {
    override fun digest(c: Hasher): Hash =
        c.applyHash(
            tag.encodeStringToUTF8() + ledgerHash.bytes
        )

    internal constructor(
        tag: String,
        ledgerHash: Hash,
        hasher: Hasher
    ) : this(
        tag, ledgerHash,
        generateChainHandleHash(hasher, tag, ledgerHash)
    )


    override fun toString(): String = """
        |       ChainId {
        |           Tag: $tag
        |           Ledger: ${ledgerHash.print}
        |           Hash: ${hashId.print}
        |       }
    """.trimMargin()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerId) return false

        if (tag != other.tag) return false
        if (!hashId.contentEquals(other.hashId)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + hashId.hashCode()
        return result
    }

    companion object {
        private fun generateChainHandleHash(
            hasher: Hasher,
            tag: String,
            ledgerHash: Hash
        ): Hash =
            hasher.applyHash(
                tag.encodeStringToUTF8() + ledgerHash.bytes
            )
    }

}
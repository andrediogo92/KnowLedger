package org.knowledger.ledger.config.chainid

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.base64Encode

@JsonClass(generateAdapter = true)
class StorageUnawareChainId internal constructor(
    override val tag: Tag,
    override val ledgerHash: Hash,
    override val hashId: Hash
) : ChainId {
    override fun digest(c: Hasher): Hash =
        generateChainHandleHash(c, tag, ledgerHash)

    internal constructor(
        tag: Tag,
        ledgerHash: Hash,
        hasher: Hasher
    ) : this(
        tag, ledgerHash,
        generateChainHandleHash(
            hasher,
            tag,
            ledgerHash
        )
    )


    override fun toString(): String = """
        |       ChainId {
        |           Tag: ${tag.base64Encode()}
        |           Ledger: ${ledgerHash.print}
        |           Hash: ${hashId.print}
        |       }
    """.trimMargin()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StorageUnawareChainId) return false

        if (tag.contentEquals(other.tag)) return false
        if (ledgerHash.contentEquals(other.ledgerHash)) return false
        if (hashId.contentEquals(other.hashId)) return false

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
            tag: Tag,
            ledgerHash: Hash
        ): Hash =
            hasher.applyHash(
                tag.bytes + ledgerHash.bytes
            )
    }

}
package pt.um.masb.ledger.config

import com.squareup.moshi.JsonClass
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hashed
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.encodeStringToUTF8
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.ledger.service.ServiceClass
import java.time.Instant
import java.util.*

@JsonClass(generateAdapter = true)
data class LedgerId internal constructor(
    val tag: String,
    override val hashId: Hash
) : Hashed, ServiceClass {

    internal constructor(
        tag: String,
        hasher: Hasher
    ) : this(tag, generateLedgerHandleHash(hasher, tag))


    override fun toString(): String = """
        |       LedgerId {
        |           Tag: $tag
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
        private fun generateLedgerHandleHash(
            hasher: Hasher,
            tag: String
        ): Hash =
            hasher.applyHash(
                flattenBytes(
                    tag.encodeStringToUTF8(),
                    UUID.randomUUID().bytes(),
                    Instant.now().bytes()
                )
            )
    }
}
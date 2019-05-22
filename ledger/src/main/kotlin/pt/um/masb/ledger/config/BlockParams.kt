package pt.um.masb.ledger.config

import com.squareup.moshi.JsonClass
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hashable
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.storage.LedgerContract

@JsonClass(generateAdapter = true)
data class BlockParams(
    val blockMemSize: Long = 2097152,
    val blockLength: Long = 512
) : Hashable, LedgerContract {


    override fun digest(c: Hasher): Hash =
        c.applyHash(
            blockMemSize.bytes() +
                    blockLength.bytes()
        )

}
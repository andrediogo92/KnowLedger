package org.knowledger.ledger.config

import com.squareup.moshi.JsonClass
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hashable
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.bytes
import org.knowledger.ledger.service.ServiceClass

@JsonClass(generateAdapter = true)
data class BlockParams(
    val blockMemSize: Long = 2097152,
    val blockLength: Long = 512
) : Hashable, ServiceClass {


    override fun digest(c: Hasher): Hash =
        c.applyHash(
            blockMemSize.bytes() +
                    blockLength.bytes()
        )

}
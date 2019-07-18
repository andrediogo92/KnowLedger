package org.knowledger.ledger.config

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashable
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.bytes
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
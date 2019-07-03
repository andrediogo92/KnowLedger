package org.knowledger.ledger.config

import com.squareup.moshi.JsonClass
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hashable
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.bytes
import org.knowledger.common.misc.flattenBytes
import org.knowledger.ledger.service.ServiceClass

@JsonClass(generateAdapter = true)
data class LedgerParams(
    val crypter: Hash,
    val recalcTime: Long = 1228800000,
    val recalcTrigger: Long = 2048,
    val blockParams: BlockParams = BlockParams()
) : Hashable, ServiceClass {

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                crypter.bytes,
                recalcTime.bytes(),
                recalcTrigger.bytes(),
                blockParams.blockMemSize.bytes(),
                blockParams.blockLength.bytes()
            )
        )
}
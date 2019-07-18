package org.knowledger.ledger.config

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashable
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.bytes
import org.knowledger.ledger.core.misc.flattenBytes
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
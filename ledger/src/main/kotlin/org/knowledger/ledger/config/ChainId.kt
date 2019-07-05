package org.knowledger.ledger.config

import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hashable
import org.knowledger.common.hash.Hashed
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.encodeStringToUTF8
import org.knowledger.ledger.service.ServiceClass

interface ChainId : Hashed, Hashable, ServiceClass {
    val tag: String
    val ledgerHash: Hash
    override val hashId: Hash

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            tag.encodeStringToUTF8() + ledgerHash.bytes
        )
}
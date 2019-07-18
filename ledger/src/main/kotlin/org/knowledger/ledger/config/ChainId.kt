package org.knowledger.ledger.config

import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashable
import org.knowledger.ledger.core.hash.Hashed
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.encodeStringToUTF8
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
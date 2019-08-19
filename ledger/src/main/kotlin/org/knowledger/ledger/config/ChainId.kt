package org.knowledger.ledger.config

import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashable
import org.knowledger.ledger.core.hash.Hashed
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.service.ServiceClass

interface ChainId : Hashed, Hashable, ServiceClass {
    val tag: Tag
    val ledgerHash: Hash
    override val hashId: Hash

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            tag.bytes + ledgerHash.bytes
        )
}
package org.knowledger.ledger.config

import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.service.ServiceClass

interface ChainId : Hashing, ServiceClass {
    val tag: Tag
    val ledgerHash: Hash
    override val hash: Hash
}
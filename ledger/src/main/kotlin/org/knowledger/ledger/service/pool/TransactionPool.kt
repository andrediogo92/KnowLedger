package org.knowledger.ledger.service.pool

import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.service.ServiceClass

interface TransactionPool : ServiceClass {
    val transactions: List<Hash>
    val confirmations: List<Boolean>
    val unconfirmed: List<Hash>
    val firstUnconfirmed: Hash?
}
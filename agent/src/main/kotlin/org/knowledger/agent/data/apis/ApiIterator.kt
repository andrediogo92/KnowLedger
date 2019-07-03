package org.knowledger.agent.data.apis

import org.knowledger.ledger.storage.Transaction


interface ApiIterator : Iterator<Transaction> {

    val transactions: Collection<Transaction>
}
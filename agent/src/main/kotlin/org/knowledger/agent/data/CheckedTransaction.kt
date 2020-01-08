package org.knowledger.agent.data

import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.storage.Transaction

data class CheckedTransaction(
    val tag: Tag, val transaction: Transaction
)
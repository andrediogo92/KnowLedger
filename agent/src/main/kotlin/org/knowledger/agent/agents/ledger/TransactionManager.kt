package org.knowledger.agent.agents.ledger

import org.knowledger.agent.data.CheckedTransaction
import org.knowledger.ledger.config.GlobalLedgerConfiguration.CACHE_SIZE
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

data class TransactionManager(
    val checkedTransactions: BlockingQueue<CheckedTransaction> =
        ArrayBlockingQueue<CheckedTransaction>(CACHE_SIZE)
) : BlockingQueue<CheckedTransaction> by checkedTransactions {
}
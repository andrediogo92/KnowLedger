package org.knowledger.ledger.chain.service

import org.knowledger.ledger.chain.ChainInfo
import org.knowledger.ledger.chain.data.WitnessReference
import org.knowledger.ledger.chain.transactions.QueryManager
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.results.LoadFailure

internal interface TransactionService {
    fun calculateTransactionDifference(
        block: MutableBlock, newTransaction: MutableTransaction,
        queryManager: QueryManager, chainInfo: ChainInfo, witnessService: WitnessService,
    ): Outcome<WitnessReference, LoadFailure>
}
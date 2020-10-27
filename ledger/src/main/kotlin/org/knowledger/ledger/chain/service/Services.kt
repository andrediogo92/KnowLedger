package org.knowledger.ledger.chain.service

internal data class Services(
    val coinbaseService: CoinbaseService = CoinbaseServiceImpl(),
    val transactionService: TransactionService = TransactionServiceImpl(),
    val witnessService: WitnessService = WitnessServiceImpl(),
)
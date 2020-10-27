package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.chain.data.TransactionWithBlockHash
import org.knowledger.ledger.chain.data.WitnessInfo

internal interface ServiceLoadableCollection {
    val witnessInfoServiceLoadable: ServiceLoadable<WitnessInfo>
    val transactionWithBlockHashStorageLoadable: ServiceLoadable<TransactionWithBlockHash>
}